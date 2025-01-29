package com.example.cricketcounter.ui.fragments.teamsFragment.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TeamsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}