package com.example.cricketcounter.data.models

data class Team(
    val id: Int = 0,
    val name: String,
    val matches: Int = 0,
    val won: Int = 0,
    val lost: Int = 0
)