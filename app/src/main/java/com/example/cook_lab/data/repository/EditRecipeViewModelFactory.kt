package com.example.cook_lab.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cook_lab.repository.CreateRecipeRepository
import com.example.cook_lab.viewmodel.EditRecipeViewModel

class EditRecipeViewModelFactory(
    private val repository: CreateRecipeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditRecipeViewModel::class.java)) {
            return EditRecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
