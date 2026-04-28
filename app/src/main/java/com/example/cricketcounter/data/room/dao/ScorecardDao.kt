package com.example.cricketcounter.data.room.dao

import androidx.room.*
import com.example.cricketcounter.data.models.BallEvent
import com.example.cricketcounter.data.models.BattingEntry
import com.example.cricketcounter.data.models.BowlingEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BallEventDao {
    @Insert
    suspend fun insertBall(ball: BallEvent): Long

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inning = :inning ORDER BY totalBallIndex ASC")
    fun getBallsForInnings(matchId: Int, inning: Int): Flow<List<BallEvent>>

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inning = :inning AND overNumber = :overNo ORDER BY totalBallIndex ASC")
    suspend fun getBallsForOver(matchId: Int, inning: Int, overNo: Int): List<BallEvent>

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inning = :inning ORDER BY totalBallIndex ASC")
    suspend fun getAllBallsForInnings(matchId: Int, inning: Int): List<BallEvent>

    @Query("DELETE FROM ball_events WHERE id = (SELECT MAX(id) FROM ball_events WHERE matchId = :matchId AND inning = :inning)")
    suspend fun deleteLastBall(matchId: Int, inning: Int)

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inning = :inning ORDER BY totalBallIndex DESC LIMIT 1")
    suspend fun getLastBall(matchId: Int, inning: Int): BallEvent?
}

@Dao
interface BattingEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: BattingEntry): Long

    @Update
    suspend fun update(entry: BattingEntry)

    @Query("SELECT * FROM batting_entries WHERE matchId = :matchId AND inning = :inning ORDER BY battingOrder ASC")
    fun getBattingForInnings(matchId: Int, inning: Int): Flow<List<BattingEntry>>

    @Query("SELECT * FROM batting_entries WHERE matchId = :matchId AND inning = :inning ORDER BY battingOrder ASC")
    suspend fun getBattingForInningsOnce(matchId: Int, inning: Int): List<BattingEntry>

    @Query("SELECT * FROM batting_entries WHERE matchId = :matchId AND inning = :inning AND playerName = :name LIMIT 1")
    suspend fun getEntry(matchId: Int, inning: Int, name: String): BattingEntry?
}

@Dao
interface BowlingEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: BowlingEntry): Long

    @Update
    suspend fun update(entry: BowlingEntry)

    @Query("SELECT * FROM bowling_entries WHERE matchId = :matchId AND inning = :inning ORDER BY bowlingOrder ASC")
    fun getBowlingForInnings(matchId: Int, inning: Int): Flow<List<BowlingEntry>>

    @Query("SELECT * FROM bowling_entries WHERE matchId = :matchId AND inning = :inning ORDER BY bowlingOrder ASC")
    suspend fun getBowlingForInningsOnce(matchId: Int, inning: Int): List<BowlingEntry>

    @Query("SELECT * FROM bowling_entries WHERE matchId = :matchId AND inning = :inning AND playerName = :name LIMIT 1")
    suspend fun getEntry(matchId: Int, inning: Int, name: String): BowlingEntry?
}
