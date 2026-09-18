package com.nexora.player.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Manual DI has no code-generated factories, so every ViewModel is created with
 * this one-liner instead: `viewModel(factory = GenericViewModelFactory { HomeViewModel(app.videoRepository) })`.
 */
class GenericViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
