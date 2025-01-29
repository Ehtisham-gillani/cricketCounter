package com.example.cricketcounter.ui.fragments.matchScoringFragment.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MatchScoringViewModelFactory(
    private val application: Application,
    private val matchId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchScoringViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MatchScoringViewModel(application, matchId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}