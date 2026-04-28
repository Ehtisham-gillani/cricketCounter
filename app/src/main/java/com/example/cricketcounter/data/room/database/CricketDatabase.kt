package com.example.cricketcounter.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cricketcounter.data.extensions.Converters
import com.example.cricketcounter.data.models.BallEvent
import com.example.cricketcounter.data.models.BattingEntry
import com.example.cricketcounter.data.models.BowlingEntry
import com.example.cricketcounter.data.models.Match
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.data.models.Team
import com.example.cricketcounter.data.room.dao.BallEventDao
import com.example.cricketcounter.data.room.dao.BattingEntryDao
import com.example.cricketcounter.data.room.dao.BowlingEntryDao
import com.example.cricketcounter.data.room.dao.MatchDao
import com.example.cricketcounter.data.room.dao.PlayerDao
import com.example.cricketcounter.data.room.dao.TeamDao

@Database(
    entities = [
        Team::class,
        Player::class,
        Match::class,
        BallEvent::class,
        BattingEntry::class,
        BowlingEntry::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CricketDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun ballEventDao(): BallEventDao
    abstract fun battingEntryDao(): BattingEntryDao
    abstract fun bowlingEntryDao(): BowlingEntryDao

    companion object {
        @Volatile
        private var INSTANCE: CricketDatabase? = null

        /** Migration 1→2: Drop old matches table, recreate with full event-sourced schema. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `matches`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `matches` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `battingTeamId` INTEGER NOT NULL,
                        `bowlingTeamId` INTEGER NOT NULL,
                        `battingTeamName` TEXT NOT NULL DEFAULT '',
                        `bowlingTeamName` TEXT NOT NULL DEFAULT '',
                        `totalOvers` INTEGER NOT NULL,
                        `inning` INTEGER NOT NULL DEFAULT 1,
                        `totalScore` INTEGER NOT NULL DEFAULT 0,
                        `wickets` INTEGER NOT NULL DEFAULT 0,
                        `completedOvers` INTEGER NOT NULL DEFAULT 0,
                        `ballsInCurrentOver` INTEGER NOT NULL DEFAULT 0,
                        `totalBallsBowled` INTEGER NOT NULL DEFAULT 0,
                        `striker` TEXT NOT NULL DEFAULT '',
                        `nonStriker` TEXT NOT NULL DEFAULT '',
                        `currentBowler` TEXT NOT NULL DEFAULT '',
                        `previousBowler` TEXT NOT NULL DEFAULT '',
                        `strikerRuns` INTEGER NOT NULL DEFAULT 0,
                        `strikerBalls` INTEGER NOT NULL DEFAULT 0,
                        `strikerFours` INTEGER NOT NULL DEFAULT 0,
                        `strikerSixes` INTEGER NOT NULL DEFAULT 0,
                        `nonStrikerRuns` INTEGER NOT NULL DEFAULT 0,
                        `nonStrikerBalls` INTEGER NOT NULL DEFAULT 0,
                        `nonStrikerFours` INTEGER NOT NULL DEFAULT 0,
                        `nonStrikerSixes` INTEGER NOT NULL DEFAULT 0,
                        `bowlerCompletedOvers` INTEGER NOT NULL DEFAULT 0,
                        `bowlerBallsInOver` INTEGER NOT NULL DEFAULT 0,
                        `bowlerMaidens` INTEGER NOT NULL DEFAULT 0,
                        `bowlerRuns` INTEGER NOT NULL DEFAULT 0,
                        `bowlerWickets` INTEGER NOT NULL DEFAULT 0,
                        `currentOverBalls` TEXT NOT NULL DEFAULT '',
                        `targetRuns` INTEGER,
                        `isCompleted` INTEGER NOT NULL DEFAULT 0,
                        `currentInningsEnded` INTEGER NOT NULL DEFAULT 0,
                        `nextBattingOrder` INTEGER NOT NULL DEFAULT 1,
                        `bowlingOrderCount` INTEGER NOT NULL DEFAULT 0,
                        `nextBallIsFreeHit` INTEGER NOT NULL DEFAULT 0,
                        `extrasWides` INTEGER NOT NULL DEFAULT 0,
                        `extrasNoBalls` INTEGER NOT NULL DEFAULT 0,
                        `extrasByes` INTEGER NOT NULL DEFAULT 0,
                        `extrasLegByes` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ball_events` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `matchId` INTEGER NOT NULL,
                        `inning` INTEGER NOT NULL,
                        `overNumber` INTEGER NOT NULL,
                        `ballNumber` INTEGER NOT NULL,
                        `totalBallIndex` INTEGER NOT NULL,
                        `runs` INTEGER NOT NULL DEFAULT 0,
                        `extraRuns` INTEGER NOT NULL DEFAULT 0,
                        `extraType` TEXT NOT NULL DEFAULT '',
                        `isWicket` INTEGER NOT NULL DEFAULT 0,
                        `wicketType` TEXT NOT NULL DEFAULT '',
                        `isBowlerWicket` INTEGER NOT NULL DEFAULT 0,
                        `outBatsmanName` TEXT NOT NULL DEFAULT '',
                        `fielderName` TEXT NOT NULL DEFAULT '',
                        `strikerName` TEXT NOT NULL DEFAULT '',
                        `nonStrikerName` TEXT NOT NULL DEFAULT '',
                        `bowlerName` TEXT NOT NULL DEFAULT '',
                        `isLegalDelivery` INTEGER NOT NULL DEFAULT 1,
                        `isFreeHit` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `batting_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `matchId` INTEGER NOT NULL,
                        `inning` INTEGER NOT NULL,
                        `playerName` TEXT NOT NULL,
                        `battingOrder` INTEGER NOT NULL,
                        `runs` INTEGER NOT NULL DEFAULT 0,
                        `ballsFaced` INTEGER NOT NULL DEFAULT 0,
                        `fours` INTEGER NOT NULL DEFAULT 0,
                        `sixes` INTEGER NOT NULL DEFAULT 0,
                        `isOut` INTEGER NOT NULL DEFAULT 0,
                        `dismissalType` TEXT NOT NULL DEFAULT '',
                        `dismissedBy` TEXT NOT NULL DEFAULT '',
                        `fielder` TEXT NOT NULL DEFAULT '',
                        `isNotOut` INTEGER NOT NULL DEFAULT 0,
                        `didBat` INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bowling_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `matchId` INTEGER NOT NULL,
                        `inning` INTEGER NOT NULL,
                        `playerName` TEXT NOT NULL,
                        `bowlingOrder` INTEGER NOT NULL,
                        `completedOvers` INTEGER NOT NULL DEFAULT 0,
                        `ballsInCurrentOver` INTEGER NOT NULL DEFAULT 0,
                        `maidens` INTEGER NOT NULL DEFAULT 0,
                        `runs` INTEGER NOT NULL DEFAULT 0,
                        `wickets` INTEGER NOT NULL DEFAULT 0,
                        `wides` INTEGER NOT NULL DEFAULT 0,
                        `noBalls` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration 2→3:
         * - matches: add inn1 snapshot columns + battingTeamXI + bowlingTeamXI
         * - ball_events: add outBatsmanName + fielderName columns
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Inn1 snapshot fields on matches
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1Score` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1Wickets` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1CompletedOvers` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1BallsInOver` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1ExtrasWides` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1ExtrasNoBalls` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1ExtrasByes` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `inn1ExtrasLegByes` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `battingTeamXI` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `matches` ADD COLUMN `bowlingTeamXI` TEXT NOT NULL DEFAULT ''")
                // New ball_event fields
                db.execSQL("ALTER TABLE `ball_events` ADD COLUMN `outBatsmanName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `ball_events` ADD COLUMN `fielderName` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): CricketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CricketDatabase::class.java,
                    "cricket_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}