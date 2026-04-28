package com.example.cricketcounter.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single source of truth for every ball bowled.
 * All scorecard stats are derived from this table.
 */
@Entity(tableName = "ball_events")
data class BallEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matchId: Int,
    val inning: Int,           // 1 or 2
    val overNumber: Int,       // 0-indexed completed over (0 = first over)
    val ballNumber: Int,       // 1-indexed legal ball in the over (1-6)
    val totalBallIndex: Int,   // absolute ball number for ordering (includes extras)
    val runs: Int = 0,         // runs scored off bat (excluding penalty)
    val extraRuns: Int = 0,    // penalty run (1 for wide/no-ball)
    val extraType: String = "", // "WIDE","NO_BALL","BYE","LEG_BYE" or ""
    val isWicket: Boolean = false,
    val wicketType: String = "", // "BOWLED","CAUGHT","LBW","RUN_OUT","STUMPED","HIT_WICKET"
    val isBowlerWicket: Boolean = false, // false if RUN_OUT
    val outBatsmanName: String = "",     // who actually got out (striker OR non-striker for run-outs)
    val fielderName: String = "",        // catcher / stumping keeper / run-out thrower
    val strikerName: String = "",
    val nonStrikerName: String = "",
    val bowlerName: String = "",
    val isLegalDelivery: Boolean = true, // false for WIDE / NO_BALL
    val isFreeHit: Boolean = false       // ball after a no-ball
)
