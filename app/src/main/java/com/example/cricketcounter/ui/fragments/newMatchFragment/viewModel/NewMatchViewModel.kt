package com.example.cricketcounter.ui.fragments.newMatchFragment.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.cricketcounter.data.models.Match
import com.example.cricketcounter.data.models.MatchDetails
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.data.models.Team
import com.example.cricketcounter.data.room.database.CricketDatabase
import kotlinx.coroutines.flow.Flow

class NewMatchViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CricketDatabase.getDatabase(application)
    private val teamDao = database.teamDao()
    private val playerDao = database.playerDao()
    private val matchDao = database.matchDao()

    private var _matchDetails: MatchDetails? = null
    val matchDetails: MatchDetails?
        get() = _matchDetails

    data class MatchSetup(
        val team1Id: Int,
        val team2Id: Int,
        val team1Name: String,
        val team2Name: String,
        val tossWonByTeam1: Boolean,
        val choseToBat: Boolean,
        val overs: Int
    )

    suspend fun addTeamsForMatch(team1Name: String, team2Name: String, overs: Int): Pair<Int, Int> {
        try {
            val team1Id = teamDao.getTeamByName(team1Name)?.let { team ->
                teamDao.incrementMatches(team.id)
                team.id
            } ?: teamDao.insertTeam(Team(name = team1Name)).toInt()

            val team2Id = teamDao.getTeamByName(team2Name)?.let { team ->
                teamDao.incrementMatches(team.id)
                team.id
            } ?: teamDao.insertTeam(Team(name = team2Name)).toInt()

            return Pair(team1Id, team2Id)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun addOrUpdatePlayers(
        striker: String,
        nonStriker: String,
        bowler: String,
        matchSetup: MatchSetup
    ) {
        // Calculate batting and bowling teams
        val (battingTeamId, bowlingTeamId) = if (matchSetup.tossWonByTeam1) {
            if (matchSetup.choseToBat) {
                Pair(matchSetup.team1Id, matchSetup.team2Id)
            } else {
                Pair(matchSetup.team2Id, matchSetup.team1Id)
            }
        } else {
            if (matchSetup.choseToBat) {
                Pair(matchSetup.team2Id, matchSetup.team1Id)
            } else {
                Pair(matchSetup.team1Id, matchSetup.team2Id)
            }
        }

        // Similarly for team names
        val (battingTeamName, bowlingTeamName) = if (matchSetup.tossWonByTeam1) {
            if (matchSetup.choseToBat) {
                Pair(matchSetup.team1Name, matchSetup.team2Name)
            } else {
                Pair(matchSetup.team2Name, matchSetup.team1Name)
            }
        } else {
            if (matchSetup.choseToBat) {
                Pair(matchSetup.team2Name, matchSetup.team1Name)
            } else {
                Pair(matchSetup.team1Name, matchSetup.team2Name)
            }
        }

        suspend fun addOrUpdatePlayer(name: String, teamId: Int) {
            playerDao.getPlayerByNameAndTeam(name, teamId)?.let { existingPlayer ->
                playerDao.incrementMatches(existingPlayer.id)
            } ?: playerDao.insertPlayer(Player(name = name, teamId = teamId))
        }

        addOrUpdatePlayer(striker, battingTeamId)
        addOrUpdatePlayer(nonStriker, battingTeamId)
        addOrUpdatePlayer(bowler, bowlingTeamId)

        val match = Match(
            battingTeamId = battingTeamId,
            bowlingTeamId = bowlingTeamId,
            totalOvers = matchSetup.overs,
            striker = striker,
            nonStriker = nonStriker,
            currentBowler = bowler
        )
        val matchId = matchDao.insertMatch(match).toInt()

        _matchDetails = MatchDetails(
            matchId = matchId,
            battingTeam = battingTeamName,
            bowlingTeam = bowlingTeamName,
            striker = striker,
            nonStriker = nonStriker,
            bowler = bowler,
            overs = matchSetup.overs
        )
    }
}