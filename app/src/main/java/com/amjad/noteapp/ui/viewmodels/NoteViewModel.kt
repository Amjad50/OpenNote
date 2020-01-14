package com.amjad.noteapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.data.NoteDatabase
import com.amjad.noteapp.repositories.NotesRepository
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotesRepository

    val allNotes: LiveData<List<Note>>
    private val selectedNote = MutableLiveData<Long>()
    val note: LiveData<Note>

    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NotesRepository(wordsDao)
        note = Transformations.switchMap(selectedNote) { id -> repository.getNote(id) }
        allNotes = repository.allNotes
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun setNoteID(id: Long) {
        selectedNote.value = id
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun deleteNotes(notesIds: List<Long>) = viewModelScope.launch {
        repository.deleteNotes(notesIds)
    }

}
