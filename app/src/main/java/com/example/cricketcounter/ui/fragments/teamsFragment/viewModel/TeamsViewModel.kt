package com.example.cricketcounter.ui.fragments.teamsFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cricketcounter.data.models.Team

class TeamsViewModel : ViewModel() {
    private val _teams = MutableLiveData<List<Team>>()
    val teams: LiveData<List<Team>> = _teams

    private val teamsList = mutableListOf<Team>()

    fun addTeam(teamName: String) {
        val newTeam = Team(
            id = teamsList.size + 1,
            name = teamName,
            matches = 0,
            won = 0,
            lost = 0
        )
        teamsList.add(newTeam)
        _teams.value = teamsList.toList()
    }

    fun updateTeam(team: Team, newName: String) {
        val index = teamsList.indexOfFirst { it.id == team.id }
        if (index != -1) {
            teamsList[index] = team.copy(name = newName)
            _teams.value = teamsList.toList()
        }
    }

    fun deleteTeam(team: Team) {
        teamsList.remove(team)
        _teams.value = teamsList.toList()
    }
}