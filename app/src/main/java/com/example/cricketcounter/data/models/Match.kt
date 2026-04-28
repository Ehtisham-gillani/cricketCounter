package com.example.cricketcounter.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Match entity — stores live match state only.
 * Full historical stats live in BattingEntry / BowlingEntry / BallEvent tables.
 *
 * Overs are stored as two Ints (completedOvers + ballsInCurrentOver) to avoid
 * floating-point arithmetic bugs (the 0.1+0.1+...≠1.0 problem).
 */
@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Teams
    val battingTeamId: Int,
    val bowlingTeamId: Int,
    val battingTeamName: String = "",
    val bowlingTeamName: String = "",
    val totalOvers: Int,

    // Innings state
    val inning: Int = 1,
    val totalScore: Int = 0,
    val wickets: Int = 0,
    val completedOvers: Int = 0,
    val ballsInCurrentOver: Int = 0,
    val totalBallsBowled: Int = 0,

    // Active players
    val striker: String = "",
    val nonStriker: String = "",
    val currentBowler: String = "",
    val previousBowler: String = "",

    // Striker live stats
    val strikerRuns: Int = 0,
    val strikerBalls: Int = 0,
    val strikerFours: Int = 0,
    val strikerSixes: Int = 0,

    // Non-striker live stats
    val nonStrikerRuns: Int = 0,
    val nonStrikerBalls: Int = 0,
    val nonStrikerFours: Int = 0,
    val nonStrikerSixes: Int = 0,

    // Current bowler live stats
    val bowlerCompletedOvers: Int = 0,
    val bowlerBallsInOver: Int = 0,
    val bowlerMaidens: Int = 0,
    val bowlerRuns: Int = 0,
    val bowlerWickets: Int = 0,

    // Ball-by-ball for current over display
    val currentOverBalls: List<String> = emptyList(),

    // Target / result
    val targetRuns: Int? = null,
    val isCompleted: Boolean = false,
    val currentInningsEnded: Boolean = false,

    // Batting / bowling order tracking
    val nextBattingOrder: Int = 1,
    val bowlingOrderCount: Int = 0,

    // Free hit flag
    val nextBallIsFreeHit: Boolean = false,

    // Extras totals for current innings
    val extrasWides: Int = 0,
    val extrasNoBalls: Int = 0,
    val extrasByes: Int = 0,
    val extrasLegByes: Int = 0,

    // ── Innings 1 Snapshot ────────────────────────────────────────────────────
    // Populated when startNextInnings() is called so scorecard is always accurate
    val inn1Score: Int = 0,
    val inn1Wickets: Int = 0,
    val inn1CompletedOvers: Int = 0,
    val inn1BallsInOver: Int = 0,
    val inn1ExtrasWides: Int = 0,
    val inn1ExtrasNoBalls: Int = 0,
    val inn1ExtrasByes: Int = 0,
    val inn1ExtrasLegByes: Int = 0,

    // ── Playing XI lists ──────────────────────────────────────────────────────
    // Used to populate player-picker dialogs during the match
    val battingTeamXI: List<String> = emptyList(),
    val bowlingTeamXI: List<String> = emptyList()
)