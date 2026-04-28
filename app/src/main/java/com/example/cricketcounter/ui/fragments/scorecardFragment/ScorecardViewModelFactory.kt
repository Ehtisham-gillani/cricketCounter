package com.example.cricketcounter.ui.fragments.scorecardFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ScorecardViewModelFactory(private val application: Application, private val matchId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ScorecardViewModel(application, matchId) as T
    }
}
