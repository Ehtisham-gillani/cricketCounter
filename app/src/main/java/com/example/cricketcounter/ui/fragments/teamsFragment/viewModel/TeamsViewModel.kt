package com.example.cricketcounter.ui.fragments.teamsFragment.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cricketcounter.data.models.Team
import com.example.cricketcounter.data.room.database.CricketDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TeamsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CricketDatabase.getDatabase(application)
    private val teamDao = database.teamDao()

    val teams: Flow<List<Team>> = teamDao.getAllTeams()

    fun addTeam(teamName: String) = viewModelScope.launch {
        val team = Team(name = teamName)
        teamDao.insertTeam(team)
    }

    fun updateTeam(team: Team, newName: String) = viewModelScope.launch {
        teamDao.updateTeam(team.copy(name = newName))
    }

    fun deleteTeam(team: Team) = viewModelScope.launch {
        teamDao.deleteTeam(team)
    }

    fun updateTeamStats(teamId: Int, matches: Int, won: Int, lost: Int) = viewModelScope.launch {
        teamDao.updateTeamStats(teamId, matches, won, lost)
    }
}