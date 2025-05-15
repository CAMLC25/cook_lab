package com.example.cook_lab.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cook_lab.repository.CreateRecipeRepository
import com.example.cook_lab.viewmodel.CreateRecipeViewModel

class CreateRecipeViewModelFactory(
    private val repository: CreateRecipeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecipeViewModel::class.java)) {
            return CreateRecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
