package com.amjad.noteapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotesListViewModel : ViewModel() {
    // TODO: change to LiveData or something from web or DB
    val notes: LiveData<List<String>> = MutableLiveData<List<String>>(listOf("welcome", "there"))
}
