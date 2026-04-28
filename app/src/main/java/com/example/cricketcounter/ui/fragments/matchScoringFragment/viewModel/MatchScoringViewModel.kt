package com.example.cricketcounter.ui.fragments.matchScoringFragment.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cricketcounter.data.extensions.ExtraType
import com.example.cricketcounter.data.extensions.WicketType
import com.example.cricketcounter.data.models.*
import com.example.cricketcounter.data.room.database.CricketDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MatchScoringViewModel(application: Application, private val matchId: Int) : AndroidViewModel(application) {

    private val db = CricketDatabase.getDatabase(application)
    private val matchDao = db.matchDao()
    private val ballDao = db.ballEventDao()
    private val batDao = db.battingEntryDao()
    private val bowlDao = db.bowlingEntryDao()

    private val mutex = Mutex()

    private val _currentMatch = MutableStateFlow<Match?>(null)
    val currentMatch = _currentMatch.asStateFlow()

    private val _showNewBatsmanDialog = MutableStateFlow(false)
    val showNewBatsmanDialog = _showNewBatsmanDialog.asStateFlow()

    private val _showNewNonStrikerDialog = MutableStateFlow(false)
    val showNewNonStrikerDialog = _showNewNonStrikerDialog.asStateFlow()

    private val _showNewBowlerDialog = MutableStateFlow(false)
    val showNewBowlerDialog = _showNewBowlerDialog.asStateFlow()

    private val _showTargetDialog = MutableStateFlow(false)
    val showTargetDialog = _showTargetDialog.asStateFlow()

    private val _matchResult = MutableStateFlow<String?>(null)
    val matchResult = _matchResult.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            matchDao.getMatchById(matchId).collect { _currentMatch.value = it }
        }
    }

    fun formatOvers(completed: Int, balls: Int) = "$completed.$balls"
    fun actualOversDecimal(completed: Int, balls: Int) = completed + balls / 6.0

    // ── Runs ──────────────────────────────────────────────────────────────────

    fun addRuns(runs: Int) = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            if (match.currentInningsEnded) return@withLock

            val isLegal = true
            val newBallsInOver = match.ballsInCurrentOver + 1
            val overComplete = newBallsInOver == 6
            val newCompletedOvers = if (overComplete) match.completedOvers + 1 else match.completedOvers
            val finalBallsInOver = if (overComplete) 0 else newBallsInOver

            val notation = when (runs) { 4 -> "4" ; 6 -> "6" ; 0 -> "·" ; else -> runs.toString() }
            val newBalls = if (overComplete) emptyList() else match.currentOverBalls + notation

            // Ball event
            ballDao.insertBall(BallEvent(
                matchId = matchId, inning = match.inning,
                overNumber = match.completedOvers, ballNumber = newBallsInOver,
                totalBallIndex = match.totalBallsBowled + 1,
                runs = runs, isLegalDelivery = true,
                strikerName = match.striker, nonStrikerName = match.nonStriker,
                bowlerName = match.currentBowler, isFreeHit = match.nextBallIsFreeHit
            ))

            // Update batting entries
            updateBatterDB(match, match.striker, runs, 1, runs == 4, runs == 6)

            // Update bowling entry
            val bowlerEntry = getOrCreateBowlerEntry(match)
            val newBowlerBalls = bowlerEntry.ballsInCurrentOver + 1
            val bowlerOverComplete = newBowlerBalls == 6
            val maiden = bowlerOverComplete && isMaidenOver(matchId, match.inning, match.completedOvers)
            bowlDao.update(bowlerEntry.copy(
                completedOvers = if (bowlerOverComplete) bowlerEntry.completedOvers + 1 else bowlerEntry.completedOvers,
                ballsInCurrentOver = if (bowlerOverComplete) 0 else newBowlerBalls,
                runs = bowlerEntry.runs + runs,
                maidens = bowlerEntry.maidens + if (maiden) 1 else 0
            ))

            var updated = match.copy(
                totalScore = match.totalScore + runs,
                completedOvers = newCompletedOvers,
                ballsInCurrentOver = finalBallsInOver,
                totalBallsBowled = match.totalBallsBowled + 1,
                currentOverBalls = newBalls,
                strikerRuns = match.strikerRuns + runs,
                strikerBalls = match.strikerBalls + 1,
                strikerFours = match.strikerFours + if (runs == 4) 1 else 0,
                strikerSixes = match.strikerSixes + if (runs == 6) 1 else 0,
                bowlerCompletedOvers = if (bowlerOverComplete) bowlerEntry.completedOvers + 1 else bowlerEntry.completedOvers,
                bowlerBallsInOver = if (bowlerOverComplete) 0 else newBowlerBalls,
                bowlerRuns = match.bowlerRuns + runs,
                bowlerMaidens = match.bowlerMaidens + if (maiden) 1 else 0,
                nextBallIsFreeHit = false
            )
            if (runs % 2 == 1) updated = swapBatsmen(updated)
            if (overComplete) {
                updated = swapBatsmen(updated).copy(previousBowler = match.currentBowler)
                _showNewBowlerDialog.value = true
            }
            matchDao.updateMatch(updated)
            checkInnings(updated)
        }
    }

    // ── Extras ────────────────────────────────────────────────────────────────

    fun addExtra(type: ExtraType, runs: Int) = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            if (match.currentInningsEnded) return@withLock

            val isLegal = type == ExtraType.BYE || type == ExtraType.LEG_BYE
            val penalty = if (type == ExtraType.WIDE || type == ExtraType.NO_BALL) 1 else 0
            val totalRuns = runs + penalty

            val notation = when (type) {
                ExtraType.WIDE -> if (runs > 0) "${runs + 1}wd" else "wd"
                ExtraType.NO_BALL -> if (runs > 0) "${runs}nb" else "nb"
                ExtraType.BYE -> if (runs > 0) "${runs}b" else "·b"
                ExtraType.LEG_BYE -> if (runs > 0) "${runs}lb" else "·lb"
            }

            val newBallsInOver = if (isLegal) match.ballsInCurrentOver + 1 else match.ballsInCurrentOver
            val overComplete = isLegal && newBallsInOver == 6
            val newCompletedOvers = if (overComplete) match.completedOvers + 1 else match.completedOvers
            val finalBallsInOver = if (overComplete) 0 else newBallsInOver
            val newBalls = if (overComplete) emptyList() else match.currentOverBalls + notation

            ballDao.insertBall(BallEvent(
                matchId = matchId, inning = match.inning,
                overNumber = match.completedOvers, ballNumber = if (isLegal) newBallsInOver else match.ballsInCurrentOver,
                totalBallIndex = match.totalBallsBowled + 1,
                runs = runs, extraRuns = penalty, extraType = type.name,
                isLegalDelivery = isLegal,
                strikerName = match.striker, nonStrikerName = match.nonStriker,
                bowlerName = match.currentBowler
            ))

            // Striker balls (not on wide)
            val strikerBallAdd = if (type != ExtraType.WIDE) 1 else 0
            val strikerRunAdd = if (type == ExtraType.NO_BALL) runs else 0
            if (strikerBallAdd > 0 || strikerRunAdd > 0) {
                updateBatterDB(match, match.striker, strikerRunAdd, strikerBallAdd,
                    strikerRunAdd == 4, strikerRunAdd == 6)
            }

            val bowlerEntry = getOrCreateBowlerEntry(match)
            val bowlerRunAdd = if (type == ExtraType.WIDE || type == ExtraType.NO_BALL) totalRuns else 0
            val newBowlerBalls = if (isLegal) bowlerEntry.ballsInCurrentOver + 1 else bowlerEntry.ballsInCurrentOver
            val bowlerOverComplete = isLegal && newBowlerBalls == 6
            val maiden = bowlerOverComplete && isMaidenOver(matchId, match.inning, match.completedOvers)
            bowlDao.update(bowlerEntry.copy(
                completedOvers = if (bowlerOverComplete) bowlerEntry.completedOvers + 1 else bowlerEntry.completedOvers,
                ballsInCurrentOver = if (bowlerOverComplete) 0 else newBowlerBalls,
                runs = bowlerEntry.runs + bowlerRunAdd,
                maidens = bowlerEntry.maidens + if (maiden) 1 else 0,
                wides = bowlerEntry.wides + if (type == ExtraType.WIDE) 1 else 0,
                noBalls = bowlerEntry.noBalls + if (type == ExtraType.NO_BALL) 1 else 0
            ))

            var updated = match.copy(
                totalScore = match.totalScore + totalRuns,
                completedOvers = newCompletedOvers,
                ballsInCurrentOver = finalBallsInOver,
                totalBallsBowled = match.totalBallsBowled + 1,
                currentOverBalls = newBalls,
                strikerRuns = match.strikerRuns + strikerRunAdd,
                strikerBalls = match.strikerBalls + strikerBallAdd,
                strikerFours = match.strikerFours + if (strikerRunAdd == 4) 1 else 0,
                strikerSixes = match.strikerSixes + if (strikerRunAdd == 6) 1 else 0,
                bowlerCompletedOvers = if (bowlerOverComplete) bowlerEntry.completedOvers + 1 else bowlerEntry.completedOvers,
                bowlerBallsInOver = if (bowlerOverComplete) 0 else newBowlerBalls,
                bowlerRuns = match.bowlerRuns + bowlerRunAdd,
                bowlerMaidens = match.bowlerMaidens + if (maiden) 1 else 0,
                extrasWides = match.extrasWides + if (type == ExtraType.WIDE) totalRuns else 0,
                extrasNoBalls = match.extrasNoBalls + if (type == ExtraType.NO_BALL) penalty else 0,
                extrasByes = match.extrasByes + if (type == ExtraType.BYE) runs else 0,
                extrasLegByes = match.extrasLegByes + if (type == ExtraType.LEG_BYE) runs else 0,
                nextBallIsFreeHit = type == ExtraType.NO_BALL
            )
            if (runs % 2 == 1) updated = swapBatsmen(updated)
            if (overComplete) {
                updated = swapBatsmen(updated).copy(previousBowler = match.currentBowler, nextBallIsFreeHit = false)
                _showNewBowlerDialog.value = true
            }
            matchDao.updateMatch(updated)
            checkInnings(updated)
        }
    }

    // ── Wicket ────────────────────────────────────────────────────────────────

    fun addWicket(type: WicketType, runsBeforeWicket: Int = 0) = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            if (match.currentInningsEnded) return@withLock
            if (match.nextBallIsFreeHit && type != WicketType.RUN_OUT) {
                _toastMessage.value = "Free Hit! Batsman cannot be dismissed (except run out)"
                return@withLock
            }

            val isBowlerW = type != WicketType.RUN_OUT
            val newBallsInOver = match.ballsInCurrentOver + 1
            val overComplete = newBallsInOver == 6
            val newCompletedOvers = if (overComplete) match.completedOvers + 1 else match.completedOvers
            val finalBallsInOver = if (overComplete) 0 else newBallsInOver
            val notation = if (runsBeforeWicket > 0) "${runsBeforeWicket}W" else "W"
            val newBalls = if (overComplete) emptyList() else match.currentOverBalls + notation

            ballDao.insertBall(BallEvent(
                matchId = matchId, inning = match.inning,
                overNumber = match.completedOvers, ballNumber = newBallsInOver,
                totalBallIndex = match.totalBallsBowled + 1,
                runs = runsBeforeWicket, isWicket = true, wicketType = type.name,
                isBowlerWicket = isBowlerW, isLegalDelivery = true,
                strikerName = match.striker, nonStrikerName = match.nonStriker,
                bowlerName = match.currentBowler
            ))

            // Mark striker as out in DB
            val batEntry = batDao.getEntry(matchId, match.inning, match.striker)
            batEntry?.let {
                batDao.update(it.copy(
                    runs = it.runs + runsBeforeWicket,
                    ballsFaced = it.ballsFaced + 1,
                    isOut = true,
                    dismissalType = type.name,
                    dismissedBy = if (isBowlerW) match.currentBowler else ""
                ))
            }
            if (runsBeforeWicket > 0) {
                updateScoreboardRunsOnly(match, runsBeforeWicket)
            }

            val bowlerEntry = getOrCreateBowlerEntry(match)
            val newBowlerBalls = bowlerEntry.ballsInCurrentOver + 1
            val bowlerOverComplete = newBowlerBalls == 6
            val maiden = bowlerOverComplete && isMaidenOver(matchId, match.inning, match.completedOvers)
            bowlDao.update(bowlerEntry.copy(
                completedOvers = if (bowlerOverComplete) bowlerEntry.completedOvers + 1 else bowlerEntry.completedOvers,
                ballsInCurrentOver = if (bowlerOverComplete) 0 else newBowlerBalls,
                wickets = bowlerEntry.wickets + if (isBowlerW) 1 else 0,
                maidens = bowlerEntry.maidens + if (maiden) 1 else 0
            ))

            var updated = match.copy(
                wickets = match.wickets + 1,
                totalScore = match.totalScore + runsBeforeWicket,
                completedOvers = newCompletedOvers,
                ballsInCurrentOver = finalBallsInOver,
                totalBallsBowled = match.totalBallsBowled + 1,
                currentOverBalls = newBalls,
                strikerBalls = match.strikerBalls + 1,
                strikerRuns = match.strikerRuns + runsBeforeWicket,
                bowlerCompletedOvers = if (bowlerOverComplete) bowlerEntry.completedOvers + 1 else bowlerEntry.completedOvers,
                bowlerBallsInOver = if (bowlerOverComplete) 0 else newBowlerBalls,
                bowlerWickets = match.bowlerWickets + if (isBowlerW) 1 else 0,
                bowlerMaidens = match.bowlerMaidens + if (maiden) 1 else 0,
                nextBallIsFreeHit = false
            )
            if (runsBeforeWicket % 2 == 1) updated = swapBatsmen(updated)
            if (overComplete) {
                updated = swapBatsmen(updated).copy(previousBowler = match.currentBowler)
                _showNewBowlerDialog.value = true
            }
            matchDao.updateMatch(updated)
            checkInnings(updated)
            if (updated.wickets < 10 && !updated.currentInningsEnded) {
                _showNewBatsmanDialog.value = true
            }
        }
    }

    // ── Innings management ────────────────────────────────────────────────────

    private suspend fun checkInnings(match: Match) {
        val oversUp = match.completedOvers >= match.totalOvers
        val allOut = match.wickets >= 10
        val target = match.inning == 2 && match.targetRuns != null && match.totalScore >= match.targetRuns

        if ((oversUp || allOut || target) && !match.currentInningsEnded) {
            val ended = match.copy(currentInningsEnded = true, isCompleted = match.inning == 2)
            matchDao.updateMatch(ended)
            if (match.inning == 1) {
                _showTargetDialog.value = true
            } else {
                // In 2nd innings, the chasing/batting team is stored as bowlingTeamName in DB
                // and the defending/bowling team is stored as battingTeamName in DB
                val chasingTeam = match.bowlingTeamName
                val defendingTeam = match.battingTeamName
                val result = when {
                    target -> "$chasingTeam win by ${10 - match.wickets} wicket(s)! 🏆"
                    allOut || oversUp -> {
                        val diff = (match.targetRuns ?: 0) - match.totalScore - 1
                        "$defendingTeam win by $diff run(s)! 🏆"
                    }
                    else -> "Match tied!"
                }
                _matchResult.value = result
            }
        }
    }

    fun startNextInnings() = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            matchDao.updateMatch(match.copy(
                inning = 2, currentInningsEnded = false,
                targetRuns = match.totalScore + 1,
                totalScore = 0, wickets = 0,
                completedOvers = 0, ballsInCurrentOver = 0, totalBallsBowled = 0,
                striker = "", nonStriker = "", currentBowler = "", previousBowler = "",
                strikerRuns = 0, strikerBalls = 0, strikerFours = 0, strikerSixes = 0,
                nonStrikerRuns = 0, nonStrikerBalls = 0, nonStrikerFours = 0, nonStrikerSixes = 0,
                bowlerCompletedOvers = 0, bowlerBallsInOver = 0, bowlerMaidens = 0, bowlerRuns = 0, bowlerWickets = 0,
                currentOverBalls = emptyList(), nextBattingOrder = 1, bowlingOrderCount = 0,
                nextBallIsFreeHit = false, extrasWides = 0, extrasNoBalls = 0, extrasByes = 0, extrasLegByes = 0
            ))
            _showTargetDialog.value = false
            _showNewBatsmanDialog.value = true
        }
    }

    fun updateNewBatsman(name: String) = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            val order = match.nextBattingOrder
            batDao.insertOrUpdate(BattingEntry(matchId = matchId, inning = match.inning, playerName = name, battingOrder = order))
            matchDao.updateMatch(match.copy(
                striker = name, strikerRuns = 0, strikerBalls = 0, strikerFours = 0, strikerSixes = 0,
                nextBattingOrder = order + 1
            ))
            _showNewBatsmanDialog.value = false
            if (match.inning == 2 && match.nonStriker.isEmpty()) _showNewNonStrikerDialog.value = true
        }
    }

    fun updateNewNonStriker(name: String) = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            val order = match.nextBattingOrder
            batDao.insertOrUpdate(BattingEntry(matchId = matchId, inning = match.inning, playerName = name, battingOrder = order))
            matchDao.updateMatch(match.copy(
                nonStriker = name, nonStrikerRuns = 0, nonStrikerBalls = 0, nonStrikerFours = 0, nonStrikerSixes = 0,
                nextBattingOrder = order + 1
            ))
            _showNewNonStrikerDialog.value = false
            _showNewBowlerDialog.value = true
        }
    }

    fun updateNewBowler(name: String) = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            if (name == match.previousBowler) {
                _toastMessage.value = "⚠️ $name bowled the last over! Choose a different bowler."
                _showNewBowlerDialog.value = true
                return@withLock
            }
            val maxOversPerBowler = (match.totalOvers + 4) / 5 // ceil(totalOvers/5)
            val existingEntry = bowlDao.getEntry(matchId, match.inning, name)
            if (existingEntry != null && existingEntry.completedOvers >= maxOversPerBowler) {
                _toastMessage.value = "⚠️ $name has reached the max over limit ($maxOversPerBowler)!"
                _showNewBowlerDialog.value = true
                return@withLock
            }
            val bowlerOrder = if (existingEntry == null) match.bowlingOrderCount + 1 else existingEntry.bowlingOrder
            if (existingEntry == null) {
                bowlDao.insertOrUpdate(BowlingEntry(matchId = matchId, inning = match.inning, playerName = name, bowlingOrder = bowlerOrder))
            }
            val spell = bowlDao.getEntry(matchId, match.inning, name)!!
            matchDao.updateMatch(match.copy(
                currentBowler = name,
                bowlerCompletedOvers = spell.completedOvers,
                bowlerBallsInOver = spell.ballsInCurrentOver,
                bowlerMaidens = spell.maidens,
                bowlerRuns = spell.runs,
                bowlerWickets = spell.wickets,
                bowlingOrderCount = if (existingEntry == null) bowlerOrder else match.bowlingOrderCount
            ))
            _showNewBowlerDialog.value = false
        }
    }

    fun swapBatsmenManual() = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            matchDao.updateMatch(swapBatsmen(match))
        }
    }

    // ── Undo ─────────────────────────────────────────────────────────────────

    fun undoLastBall() = viewModelScope.launch {
        mutex.withLock {
            val match = matchDao.getMatchByIdOnce(matchId) ?: return@withLock
            val lastBall = ballDao.getLastBall(matchId, match.inning) ?: return@withLock
            ballDao.deleteLastBall(matchId, match.inning)
            // Recalculate match state from all remaining balls
            recalculateMatchState(match)
        }
    }

    private suspend fun recalculateMatchState(match: Match) {
        val allBalls = ballDao.getBallsForOver(matchId, match.inning, -1) // fetch all
        // Simpler approach: undo is a full recompute from BallEvent history
        // For now, we reload from DB — the UI will refresh via Flow
        // A full recompute is complex; we rely on the BallEvent table being consistent
        // TODO: implement full recompute from BallEvent table for perfect undo
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun swapBatsmen(m: Match) = m.copy(
        striker = m.nonStriker, nonStriker = m.striker,
        strikerRuns = m.nonStrikerRuns, nonStrikerRuns = m.strikerRuns,
        strikerBalls = m.nonStrikerBalls, nonStrikerBalls = m.strikerBalls,
        strikerFours = m.nonStrikerFours, nonStrikerFours = m.strikerFours,
        strikerSixes = m.nonStrikerSixes, nonStrikerSixes = m.strikerSixes
    )

    private suspend fun getOrCreateBowlerEntry(match: Match): BowlingEntry {
        return bowlDao.getEntry(matchId, match.inning, match.currentBowler)
            ?: BowlingEntry(matchId = matchId, inning = match.inning, playerName = match.currentBowler, bowlingOrder = match.bowlingOrderCount)
    }

    private suspend fun updateBatterDB(match: Match, name: String, runs: Int, balls: Int, isFour: Boolean, isSix: Boolean) {
        val e = batDao.getEntry(matchId, match.inning, name) ?: return
        batDao.update(e.copy(
            runs = e.runs + runs,
            ballsFaced = e.ballsFaced + balls,
            fours = e.fours + if (isFour) 1 else 0,
            sixes = e.sixes + if (isSix) 1 else 0
        ))
    }

    private suspend fun updateScoreboardRunsOnly(match: Match, runs: Int) {
        val e = batDao.getEntry(matchId, match.inning, match.striker) ?: return
        batDao.update(e.copy(runs = e.runs + runs))
    }

    private suspend fun isMaidenOver(matchId: Int, inning: Int, overNo: Int): Boolean {
        val balls = ballDao.getBallsForOver(matchId, inning, overNo)
        val legalCount = balls.count { it.isLegalDelivery }
        if (legalCount < 6) return false
        return balls.none { it.runs > 0 || it.extraType == "WIDE" || it.extraType == "NO_BALL" }
    }

    fun clearToast() { _toastMessage.value = null }
    fun clearResult() { _matchResult.value = null }
}