package com.example.cricketcounter.ui.fragments.playersFragment.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.data.room.database.CricketDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class PlayersViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CricketDatabase.getDatabase(application)
    private val playerDao = database.playerDao()

    private val _currentTeamId = MutableStateFlow<Int?>(null)

    val players = _currentTeamId.flatMapLatest { teamId ->
        teamId?.let { playerDao.getPlayersForTeam(it) } ?: flowOf(emptyList())
    }.asLiveData()

    fun setTeamId(teamId: Int) {
        _currentTeamId.value = teamId
    }

    fun addPlayer(playerName: String, teamId: Int) = viewModelScope.launch {
        val player = Player(name = playerName, teamId = teamId)
        playerDao.insertPlayer(player)
    }

    fun updatePlayer(player: Player, newName: String) = viewModelScope.launch {
        playerDao.updatePlayer(player.copy(name = newName))
    }

    fun deletePlayer(player: Player) = viewModelScope.launch {
        playerDao.deletePlayer(player)
    }
}