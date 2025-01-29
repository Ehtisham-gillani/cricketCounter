package com.example.cricketcounter.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cricketcounter.data.models.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE teamId = :teamId")
    fun getPlayersForTeam(teamId: Int): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Int): Player?

    @Insert
    suspend fun insertPlayer(player: Player)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players WHERE name = :playerName AND teamId = :teamId LIMIT 1")
    suspend fun getPlayerByNameAndTeam(playerName: String, teamId: Int): Player?

    @Query("UPDATE players SET matches = matches + 1 WHERE id = :playerId")
    suspend fun incrementMatches(playerId: Int)
}
