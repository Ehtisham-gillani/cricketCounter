package com.example.cricketcounter.ui.fragments.matchScoringFragment.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cricketcounter.data.extensions.ExtraType
import com.example.cricketcounter.data.extensions.WicketType
import com.example.cricketcounter.data.models.Match
import com.example.cricketcounter.data.room.database.CricketDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchScoringViewModel(
    application: Application,
    private val matchId: Int
) : AndroidViewModel(application) {
    private val database = CricketDatabase.getDatabase(application)
    private val matchDao = database.matchDao()
    private val playerDao = database.playerDao()

    private val _currentMatch = MutableStateFlow<Match?>(null)
    val currentMatch = _currentMatch.asStateFlow()

    private val _showTargetDialog = MutableStateFlow(false)
    val showTargetDialog = _showTargetDialog.asStateFlow()

    init {
        viewModelScope.launch {
            matchDao.getMatchById(matchId).collect {
                _currentMatch.value = it
            }
        }
    }

    private fun checkInningsCompletion(match: Match) {
        val isInningsComplete = when {
            // All wickets down
            match.wickets >= 10 -> true
            // All overs completed
            match.currentOver >= match.totalOvers -> true
            // Target achieved in 2nd innings
            match.inning == 2 && match.targetRuns != null && match.totalScore >= match.targetRuns -> true
            else -> false
        }

        android.util.Log.d("MatchScoring", "Checking completion: currentOver=${match.currentOver}, totalOvers=${match.totalOvers}, wickets=${match.wickets}, isComplete=$isInningsComplete")

        if (isInningsComplete && !match.currentInningsEnded) {
            viewModelScope.launch {
                val updatedMatch = match.copy(
                    currentInningsEnded = true,
                    isCompleted = match.inning == 2
                )
                matchDao.updateMatch(updatedMatch)

                if (match.inning == 1) {
                    _showTargetDialog.value = true
                    android.util.Log.d("MatchScoring", "Innings 1 complete, showing target dialog")
                }
            }
        }
    }

    fun startNextInnings() {
        viewModelScope.launch {
            _currentMatch.value?.let { match ->
                val targetRuns = match.totalScore + 1
                val newMatch = match.copy(
                    inning = 2,
                    currentInningsEnded = false,
                    targetRuns = targetRuns,
                    totalScore = 0,
                    wickets = 0,
                    currentOver = 0.0,
                    strikerRuns = 0,
                    strikerBalls = 0,
                    strikerFours = 0,
                    strikerSixes = 0,
                    nonStrikerRuns = 0,
                    nonStrikerBalls = 0,
                    nonStrikerFours = 0,
                    nonStrikerSixes = 0,
                    bowlerOvers = 0.0,
                    bowlerMaidens = 0,
                    bowlerRuns = 0,
                    bowlerWickets = 0,
                    currentOverBalls = emptyList()
                )
                matchDao.updateMatch(newMatch)
                _showTargetDialog.value = false
            }
        }
    }

    suspend fun startSecondInnings(striker: String, nonStriker: String, bowler: String) {
        _currentMatch.value?.let { match ->
            val targetRuns = match.totalScore + 1
            val updatedMatch = match.copy(
                inning = 2,
                currentInningsEnded = false,
                targetRuns = targetRuns,
                totalScore = 0,
                wickets = 0,
                currentOver = 0.0,
                striker = striker,
                nonStriker = nonStriker,
                currentBowler = bowler,
                strikerRuns = 0,
                strikerBalls = 0,
                strikerFours = 0,
                strikerSixes = 0,
                nonStrikerRuns = 0,
                nonStrikerBalls = 0,
                nonStrikerFours = 0,
                nonStrikerSixes = 0,
                bowlerOvers = 0.0,
                bowlerMaidens = 0,
                bowlerRuns = 0,
                bowlerWickets = 0,
                currentOverBalls = emptyList(),
                battingTeamName = match.bowlingTeamName,
                bowlingTeamName = match.battingTeamName            )
            matchDao.updateMatch(updatedMatch)
            _showTargetDialog.value = false // Reset dialog trigger
        }
    }

    private suspend fun swapBatsmen(match: Match): Match {
        val updatedMatch = match.copy(
            striker = match.nonStriker,
            nonStriker = match.striker,
            strikerRuns = match.nonStrikerRuns,
            nonStrikerRuns = match.strikerRuns,
            strikerBalls = match.nonStrikerBalls,
            nonStrikerBalls = match.strikerBalls,
            strikerFours = match.nonStrikerFours,
            nonStrikerFours = match.strikerFours,
            strikerSixes = match.nonStrikerSixes,
            nonStrikerSixes = match.strikerSixes
        )
        matchDao.updateMatch(updatedMatch)
        return updatedMatch
    }

    fun addRuns(runs: Int, isExtra: Boolean = false) {
        viewModelScope.launch {
            _currentMatch.value?.let { match ->
                if (!match.currentInningsEnded) {
                    // Calculate balls and check if over is complete
                    val ballsInOver = getBallsInOver(match.currentOver)
                    val isOverComplete = ballsInOver >= 6

                    // Prepare updated balls list
                    val updatedBalls = if (isOverComplete) {
                        mutableListOf(runs.toString())
                    } else {
                        match.currentOverBalls.toMutableList().apply { add(runs.toString()) }
                    }

                    // Calculate new over count
                    val newCurrentOver = if (isExtra) {
                        match.currentOver
                    } else if (isOverComplete) {
                        match.currentOver.toInt() + 1.0 // Move to next over
                    } else {
                        match.currentOver + 0.1 // Increment by one ball
                    }

                    val newBowlerOvers = if (!isExtra) {
                        if (isOverComplete) match.bowlerOvers.toInt() + 1.0
                        else match.bowlerOvers + 0.1
                    } else match.bowlerOvers

                    // Update match state
                    var currentUpdatedMatch = match.copy(
                        totalScore = match.totalScore + runs,
                        currentOver = newCurrentOver,
                        currentOverBalls = updatedBalls,
                        bowlerRuns = match.bowlerRuns + runs,
                        bowlerOvers = newBowlerOvers,
                        strikerRuns = if (!isExtra) match.strikerRuns + runs else match.strikerRuns,
                        strikerBalls = if (!isExtra) match.strikerBalls + 1 else match.strikerBalls,
                        strikerFours = if (!isExtra && runs == 4) match.strikerFours + 1 else match.strikerFours,
                        strikerSixes = if (!isExtra && runs == 6) match.strikerSixes + 1 else match.strikerSixes
                    )

                    matchDao.updateMatch(currentUpdatedMatch)

                    // Handle swapping
                    if (!isExtra) {
                        if (runs % 2 == 1) {
                            currentUpdatedMatch = swapBatsmen(currentUpdatedMatch)
                        }
                        if (isOverComplete) {
                            currentUpdatedMatch = swapBatsmen(currentUpdatedMatch)
                            checkMaiden(match) // Check maiden with previous over's data
                        }
                    }

                    checkInningsCompletion(currentUpdatedMatch)
                }
            }
        }
    }

    fun addExtra(type: ExtraType, runs: Int = 0) {
        viewModelScope.launch {
            _currentMatch.value?.let { match ->
                if (!match.currentInningsEnded) {
                    val updatedBalls = match.currentOverBalls.toMutableList()
                    val extraRun = when(type) {
                        ExtraType.WIDE, ExtraType.NO_BALL -> 1
                        else -> 0
                    }
                    val notation = when(type) {
                        ExtraType.WIDE -> "${if(runs > 0) "$runs" else ""}wd"
                        ExtraType.NO_BALL -> "${if(runs > 0) "$runs" else ""}nb"
                        ExtraType.BYE -> "${runs}b"
                        ExtraType.LEG_BYE -> "${runs}lb"
                    }
                    updatedBalls.add(notation)

                    val updatedMatch = match.copy(
                        totalScore = match.totalScore + runs + extraRun,
                        currentOverBalls = updatedBalls,
                        bowlerRuns = match.bowlerRuns + runs + extraRun
                    )
                    matchDao.updateMatch(updatedMatch)

                    if ((type == ExtraType.BYE || type == ExtraType.LEG_BYE) && runs % 2 == 1) {
                        swapBatsmen()
                    }

                    checkInningsCompletion(updatedMatch)
                }
            }
        }
    }

    fun addWicket(type: WicketType) {
        viewModelScope.launch {
            _currentMatch.value?.let { match ->
                if (!match.currentInningsEnded) {  // Only proceed if innings not ended
                    val updatedBalls = match.currentOverBalls.toMutableList()
                    updatedBalls.add("W")

                    val updatedMatch = match.copy(
                        wickets = match.wickets + 1,
                        currentOver = updateOverCount(match.currentOver),
                        bowlerOvers = updateOverCount(match.bowlerOvers),
                        currentOverBalls = updatedBalls,
                        strikerBalls = match.strikerBalls + 1,
                        bowlerWickets = match.bowlerWickets + 1
                    )
                    matchDao.updateMatch(updatedMatch)

                    if (getBallsInOver(updatedMatch.currentOver) == 6) {
                        swapBatsmen()
                        checkMaiden(updatedMatch)
                    }

                    // Check innings completion after updating
                    checkInningsCompletion(updatedMatch)
                }
            }
        }
    }

    private fun checkMaiden(match: Match) {
        if (match.currentOverBalls.all {
                it == "0" || it == "W" || it.endsWith("b") || it.endsWith("lb")
            }) {
            viewModelScope.launch {
                matchDao.updateMatch(match.copy(
                    bowlerMaidens = match.bowlerMaidens + 1
                ))
            }
        }
    }

    private fun getBallsInOver(overs: Double): Int {
        return ((overs - overs.toInt()) * 10).toInt()
    }

    private fun updateOverCount(currentOver: Double): Double {
        val balls = getBallsInOver(currentOver)
        return if (balls < 5) {
            currentOver + 0.1
        } else {
            currentOver.toInt() + 1.0
        }
    }

    suspend fun swapBatsmen() {
        _currentMatch.value?.let { match ->
            val updatedMatch = match.copy(
                striker = match.nonStriker,
                nonStriker = match.striker,
                strikerRuns = match.nonStrikerRuns,
                nonStrikerRuns = match.strikerRuns,
                strikerBalls = match.nonStrikerBalls,
                nonStrikerBalls = match.strikerBalls,
                strikerFours = match.nonStrikerFours,
                nonStrikerFours = match.strikerFours,
                strikerSixes = match.nonStrikerSixes,
                nonStrikerSixes = match.strikerSixes
            )
            matchDao.updateMatch(updatedMatch)
        }
    }
}