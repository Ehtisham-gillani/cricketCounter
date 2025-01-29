package com.example.cricketcounter.data.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cricketcounter.data.extensions.Converters
import com.example.cricketcounter.data.models.Match
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.data.models.Team
import com.example.cricketcounter.data.room.dao.MatchDao
import com.example.cricketcounter.data.room.dao.PlayerDao
import com.example.cricketcounter.data.room.dao.TeamDao

@Database(
    entities = [Team::class, Player::class, Match::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class CricketDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: CricketDatabase? = null

        fun getDatabase(context: Context): CricketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CricketDatabase::class.java,
                    "cricket_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}