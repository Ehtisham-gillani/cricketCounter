package com.example.cricketcounter.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val battingTeamId: Int,
    val bowlingTeamId: Int,
    val totalOvers: Int,
    val currentOver: Double = 0.0,
    val totalScore: Int = 0,
    val wickets: Int = 0,
    val striker: String,
    val nonStriker: String,
    val currentBowler: String,
    val inning: Int = 1,
    // Additional fields for detailed stats
    val strikerRuns: Int = 0,
    val strikerBalls: Int = 0,
    val strikerFours: Int = 0,
    val strikerSixes: Int = 0,
    val nonStrikerRuns: Int = 0,
    val nonStrikerBalls: Int = 0,
    val nonStrikerFours: Int = 0,
    val nonStrikerSixes: Int = 0,
    val bowlerOvers: Double = 0.0,
    val bowlerMaidens: Int = 0,
    val bowlerRuns: Int = 0,
    val bowlerWickets: Int = 0,
    val currentOverBalls: List<String> = emptyList(),
    val targetRuns: Int? = null,
    val targetOvers: Int? = null,
    val isCompleted: Boolean = false,
    val currentInningsEnded: Boolean = false
)