package com.amjad.noteapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.data.NoteDatabase
import com.amjad.noteapp.repositories.NotesRepository
import kotlinx.coroutines.launch

class NotesListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NotesRepository

    val allNotes: LiveData<List<Note>>

    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NotesRepository(wordsDao)
        allNotes = repository.allNotes
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }
}
