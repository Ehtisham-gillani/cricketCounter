package com.example.cricketcounter.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-innings batting record for a single player.
 * One row per batsman per innings.
 */
@Entity(tableName = "batting_entries")
data class BattingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matchId: Int,
    val inning: Int,
    val playerName: String,
    val battingOrder: Int,      // 1 = first to bat, 2 = next, etc.
    val runs: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissalType: String = "",  // e.g. "BOWLED", "CAUGHT", etc.
    val dismissedBy: String = "",    // bowler name
    val fielder: String = "",        // for caught/stumped/run-out
    val isNotOut: Boolean = false,
    val didBat: Boolean = true
)
