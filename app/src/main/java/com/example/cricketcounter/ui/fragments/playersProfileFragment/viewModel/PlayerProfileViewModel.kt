package com.example.cricketcounter.ui.fragments.playersProfileFragment.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.data.room.database.CricketDatabase
import kotlinx.coroutines.launch

class PlayerProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CricketDatabase.getDatabase(application)
    private val playerDao = database.playerDao()

    private val _player = MutableLiveData<Player>()
    val player: LiveData<Player> = _player

    fun loadPlayer(playerId: Int) = viewModelScope.launch {
        // Add this function to PlayerDao
        playerDao.getPlayerById(playerId)?.let { player ->
            _player.value = player
        }
    }

    fun updatePlayerStats(player: Player) = viewModelScope.launch {
        playerDao.updatePlayer(player)
    }
}