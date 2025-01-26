package com.example.cricketcounter.ui.fragments.playersFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cricketcounter.data.models.Player

class PlayersViewModel : ViewModel() {
    private val _players = MutableLiveData<List<Player>>()
    val players: LiveData<List<Player>> = _players

    private val playersList = mutableListOf<Player>()

    fun addPlayer(playerName: String, teamId: Int) {
        val newPlayer = Player(
            id = playersList.size + 1,
            name = playerName,
            teamId = teamId
        )
        playersList.add(newPlayer)
        _players.value = playersList.toList()
    }

    fun updatePlayer(player: Player, newName: String) {
        val index = playersList.indexOfFirst { it.id == player.id }
        if (index != -1) {
            playersList[index] = player.copy(name = newName)
            _players.value = playersList.toList()
        }
    }

    fun deletePlayer(player: Player) {
        playersList.remove(player)
        _players.value = playersList.toList()
    }

    fun getPlayersForTeam(teamId: Int) {
        _players.value = playersList.filter { it.teamId == teamId }
    }
}