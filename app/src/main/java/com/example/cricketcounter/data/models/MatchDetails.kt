package com.example.cricketcounter.data.models

data class MatchDetails(
    val matchId: Int,
    val battingTeam: String,
    val bowlingTeam: String,
    val striker: String,
    val nonStriker: String,
    val bowler: String,
    val overs: Int
)