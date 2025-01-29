package com.example.cricketcounter.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Player(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val teamId: Int,
    // Batting Stats
    val matches: Int = 0,
    val innings: Int = 0,
    val runs: Int = 0,
    val notOuts: Int = 0,
    val bestScore: Int = 0,
    val strikeRate: Double = 0.0,
    val average: Double = 0.0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val thirties: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val ducks: Int = 0,
    // Bowling Stats
    val bowlingInnings: Int = 0,
    val overs: Double = 0.0,
    val wickets: Int = 0,
    val bowlingRuns: Int = 0,
    val economyRate: Double = 0.0,
    val maidens: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0,
    val dotBalls: Int = 0,
    val fourWickets: Int = 0,
    val fiveWickets: Int = 0,
    // Fielding Stats
    val catches: Int = 0,
    val stumpings: Int = 0,
    val runOuts: Int = 0
)