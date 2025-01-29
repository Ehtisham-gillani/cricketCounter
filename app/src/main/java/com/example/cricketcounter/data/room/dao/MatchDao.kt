package com.example.cricketcounter.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cricketcounter.data.models.Match
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert
    suspend fun insertMatch(match: Match): Long

    @Update
    suspend fun updateMatch(match: Match)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchById(matchId: Int): Flow<Match>
}