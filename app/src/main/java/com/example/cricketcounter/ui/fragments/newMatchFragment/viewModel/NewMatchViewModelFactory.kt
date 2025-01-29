package com.example.cricketcounter.ui.fragments.newMatchFragment.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NewMatchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewMatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewMatchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}