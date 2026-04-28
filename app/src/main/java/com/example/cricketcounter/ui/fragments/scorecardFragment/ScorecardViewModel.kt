package com.example.cricketcounter.ui.fragments.scorecardFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cricketcounter.data.room.database.CricketDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScorecardViewModel(application: Application, private val matchId: Int) : AndroidViewModel(application) {

    private val db = CricketDatabase.getDatabase(application)
    private val matchDao = db.matchDao()
    private val batDao = db.battingEntryDao()
    private val bowlDao = db.bowlingEntryDao()

    data class BatRow(val name: String, val runs: Int, val balls: Int, val fours: Int, val sixes: Int, val dismissalType: String, val dismissedBy: String, val isNotOut: Boolean)
    data class BowlRow(val name: String, val completedOvers: Int, val balls: Int, val maidens: Int, val runs: Int, val wickets: Int)

    data class ScorecardState(
        val battingTeamName: String, val bowlingTeamName: String, val totalOvers: Int,
        val inn1BattingTeam: String, val inn1Score: Int, val inn1Wickets: Int, val inn1Overs: String,
        val inn1Batting: List<BatRow>, val inn1Bowling: List<BowlRow>, val inn1Extras: String,
        val inn2BattingTeam: String, val inn2Score: Int, val inn2Wickets: Int, val inn2Overs: String,
        val inn2Batting: List<BatRow>, val inn2Bowling: List<BowlRow>, val inn2Extras: String,
        val matchStarted2ndInnings: Boolean, val result: String
    )

    private val _scorecardState = MutableStateFlow<ScorecardState?>(null)
    val scorecardState = _scorecardState.asStateFlow()

    init {
        viewModelScope.launch {
            matchDao.getMatchById(matchId).collect { match ->
                val inn1Batting = batDao.getBattingForInningsOnce(matchId, 1).map {
                    BatRow(it.playerName, it.runs, it.ballsFaced, it.fours, it.sixes, it.dismissalType, it.dismissedBy, !it.isOut)
                }
                val inn1Bowling = bowlDao.getBowlingForInningsOnce(matchId, 1).map {
                    BowlRow(it.playerName, it.completedOvers, it.ballsInCurrentOver, it.maidens, it.runs, it.wickets)
                }
                val inn2Batting = batDao.getBattingForInningsOnce(matchId, 2).map {
                    BatRow(it.playerName, it.runs, it.ballsFaced, it.fours, it.sixes, it.dismissalType, it.dismissedBy, !it.isOut)
                }
                val inn2Bowling = bowlDao.getBowlingForInningsOnce(matchId, 2).map {
                    BowlRow(it.playerName, it.completedOvers, it.ballsInCurrentOver, it.maidens, it.runs, it.wickets)
                }

                val inn1Team = match.battingTeamName
                val inn2Team = match.bowlingTeamName
                val inn1Score = if (match.inning == 1) match.totalScore else (match.targetRuns?.minus(1) ?: 0)
                val inn1Wickets = if (match.inning == 1) match.wickets else 10.coerceAtMost((match.targetRuns ?: 0) - 1)
                val inn1OversStr = if (match.inning == 1) "${match.completedOvers}.${match.ballsInCurrentOver}" else "${match.totalOvers}.0"

                val result = if (match.isCompleted) {
                    val target = match.targetRuns ?: 0
                    // In DB: battingTeamName = 1st innings batting (defending) team
                    //         bowlingTeamName = 1st innings bowling (chasing) team
                    val chasingTeam = match.bowlingTeamName
                    val defendingTeam = match.battingTeamName
                    when {
                        match.totalScore >= target -> "$chasingTeam won by ${10 - match.wickets} wicket(s)"
                        else -> "$defendingTeam won by ${target - match.totalScore - 1} run(s)"
                    }
                } else ""

                _scorecardState.value = ScorecardState(
                    battingTeamName = match.battingTeamName,
                    bowlingTeamName = match.bowlingTeamName,
                    totalOvers = match.totalOvers,
                    inn1BattingTeam = inn1Team,
                    inn1Score = inn1Score,
                    inn1Wickets = if (match.inning == 1) match.wickets else inn1Batting.count { it.dismissalType.isNotEmpty() },
                    inn1Overs = inn1OversStr,
                    inn1Batting = inn1Batting,
                    inn1Bowling = inn1Bowling,
                    inn1Extras = buildExtrasString(match.extrasWides, match.extrasNoBalls, match.extrasByes, match.extrasLegByes),
                    inn2BattingTeam = inn2Team,
                    inn2Score = if (match.inning == 2) match.totalScore else 0,
                    inn2Wickets = if (match.inning == 2) match.wickets else 0,
                    inn2Overs = if (match.inning == 2) "${match.completedOvers}.${match.ballsInCurrentOver}" else "0.0",
                    inn2Batting = inn2Batting,
                    inn2Bowling = inn2Bowling,
                    inn2Extras = "Extras: 0",
                    matchStarted2ndInnings = match.inning == 2,
                    result = result
                )
            }
        }
    }

    private fun buildExtrasString(wides: Int, noBalls: Int, byes: Int, legByes: Int): String {
        val total = wides + noBalls + byes + legByes
        return "Extras: $total (wd $wides, nb $noBalls, b $byes, lb $legByes)"
    }
}
