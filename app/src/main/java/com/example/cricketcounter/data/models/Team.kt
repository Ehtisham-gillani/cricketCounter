package com.example.cricketcounter.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val matches: Int = 0,
    val won: Int = 0,
    val lost: Int = 0
)