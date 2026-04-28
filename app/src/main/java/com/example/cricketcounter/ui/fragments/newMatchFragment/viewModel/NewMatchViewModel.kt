package com.example.cricketcounter.ui.fragments.newMatchFragment.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.cricketcounter.data.models.*
import com.example.cricketcounter.data.room.database.CricketDatabase

class NewMatchViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CricketDatabase.getDatabase(application)
    private val teamDao = database.teamDao()
    private val playerDao = database.playerDao()
    private val matchDao = database.matchDao()
    private val batDao = database.battingEntryDao()
    private val bowlDao = database.bowlingEntryDao()

    private var _matchDetails: MatchDetails? = null
    val matchDetails: MatchDetails? get() = _matchDetails

    data class MatchSetup(
        val team1Id: Int, val team2Id: Int,
        val team1Name: String, val team2Name: String,
        val tossWonByTeam1: Boolean, val choseToBat: Boolean, val overs: Int
    )

    suspend fun addTeamsForMatch(team1Name: String, team2Name: String, overs: Int): Pair<Int, Int> {
        val team1Id = teamDao.getTeamByName(team1Name)?.let { teamDao.incrementMatches(it.id); it.id }
            ?: teamDao.insertTeam(Team(name = team1Name)).toInt()
        val team2Id = teamDao.getTeamByName(team2Name)?.let { teamDao.incrementMatches(it.id); it.id }
            ?: teamDao.insertTeam(Team(name = team2Name)).toInt()
        return Pair(team1Id, team2Id)
    }

    suspend fun addOrUpdatePlayers(striker: String, nonStriker: String, bowler: String, matchSetup: MatchSetup) {
        val (battingTeamId, bowlingTeamId) = if (matchSetup.tossWonByTeam1) {
            if (matchSetup.choseToBat) Pair(matchSetup.team1Id, matchSetup.team2Id)
            else Pair(matchSetup.team2Id, matchSetup.team1Id)
        } else {
            if (matchSetup.choseToBat) Pair(matchSetup.team2Id, matchSetup.team1Id)
            else Pair(matchSetup.team1Id, matchSetup.team2Id)
        }
        val (battingTeamName, bowlingTeamName) = if (matchSetup.tossWonByTeam1) {
            if (matchSetup.choseToBat) Pair(matchSetup.team1Name, matchSetup.team2Name)
            else Pair(matchSetup.team2Name, matchSetup.team1Name)
        } else {
            if (matchSetup.choseToBat) Pair(matchSetup.team2Name, matchSetup.team1Name)
            else Pair(matchSetup.team1Name, matchSetup.team2Name)
        }

        suspend fun addPlayer(name: String, teamId: Int) {
            playerDao.getPlayerByNameAndTeam(name, teamId)?.let { playerDao.incrementMatches(it.id) }
                ?: playerDao.insertPlayer(Player(name = name, teamId = teamId))
        }
        addPlayer(striker, battingTeamId)
        addPlayer(nonStriker, battingTeamId)
        addPlayer(bowler, bowlingTeamId)

        val match = Match(
            battingTeamId = battingTeamId, bowlingTeamId = bowlingTeamId,
            battingTeamName = battingTeamName, bowlingTeamName = bowlingTeamName,
            totalOvers = matchSetup.overs,
            striker = striker, nonStriker = nonStriker, currentBowler = bowler,
            nextBattingOrder = 3  // striker=1, nonStriker=2 already added
        )
        val matchId = matchDao.insertMatch(match).toInt()

        // Create initial batting entries
        batDao.insertOrUpdate(BattingEntry(matchId = matchId, inning = 1, playerName = striker, battingOrder = 1))
        batDao.insertOrUpdate(BattingEntry(matchId = matchId, inning = 1, playerName = nonStriker, battingOrder = 2))

        // Create initial bowling entry
        bowlDao.insertOrUpdate(BowlingEntry(matchId = matchId, inning = 1, playerName = bowler, bowlingOrder = 1))

        _matchDetails = MatchDetails(
            matchId = matchId, battingTeam = battingTeamName, bowlingTeam = bowlingTeamName,
            striker = striker, nonStriker = nonStriker, bowler = bowler, overs = matchSetup.overs
        )
    }
}