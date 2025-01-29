package com.example.cricketcounter.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cricketcounter.data.models.Team
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams")
    fun getAllTeams(): Flow<List<Team>>

    @Insert
    suspend fun insertTeam(team: Team): Long

    @Update
    suspend fun updateTeam(team: Team)

    @Delete
    suspend fun deleteTeam(team: Team)

    @Query("SELECT * FROM teams WHERE name = :teamName LIMIT 1")
    suspend fun getTeamByName(teamName: String): Team?

    @Query("UPDATE teams SET matches = matches + 1 WHERE id = :teamId")
    suspend fun incrementMatches(teamId: Int)

    @Query("UPDATE teams SET matches = matches + :matches, won = won + :won, lost = lost + :lost WHERE id = :teamId")
    suspend fun updateTeamStats(teamId: Int, matches: Int, won: Int, lost: Int)
}