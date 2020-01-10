package com.amjad.noteapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.amjad.noteapp.data.NoteDatabase
import com.amjad.noteapp.repositories.NotesRepository

class NotesEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotesRepository

    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NotesRepository(wordsDao)
    }

    private val selectedNote = MutableLiveData<Int>()
    val note = Transformations.switchMap(selectedNote) { id -> repository.getNote(id) }

    fun setNoteID(id: Int) {
        selectedNote.value = id
    }
}
