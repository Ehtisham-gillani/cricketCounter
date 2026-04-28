package com.example.cricketcounter.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-innings bowling record for a single bowler.
 * One row per bowler per innings — updated every ball.
 */
@Entity(tableName = "bowling_entries")
data class BowlingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matchId: Int,
    val inning: Int,
    val playerName: String,
    val bowlingOrder: Int,      // which bowler in sequence (1 = first, etc.)
    val completedOvers: Int = 0,
    val ballsInCurrentOver: Int = 0,
    val maidens: Int = 0,
    val runs: Int = 0,          // runs charged to bowler (no byes/leg-byes)
    val wickets: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0
)
