package com.amjad.noteapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.data.NoteDatabase
import com.amjad.noteapp.repositories.NotesRepository
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotesRepository

    private val allNotes: LiveData<List<Note>>
    private val selectedNote = MutableLiveData<Long>()
    private val toBeSaved = mutableListOf<Note>()
    private val filter = MutableLiveData<String>("")

    val filteredAllNotes: LiveData<List<Note>>
    val note: LiveData<Note>


    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()

        repository = NotesRepository(wordsDao)
        note = Transformations.switchMap(selectedNote) { id -> repository.getNote(id) }
        allNotes = repository.allNotes

        filteredAllNotes = Transformations.switchMap(filter) { filterString ->
            if (filterString.isNullOrEmpty())
                allNotes
            else
                Transformations.map(allNotes) {
                    it.filter {
                        it.title?.toLowerCase(Locale.getDefault())?.contains(filterString) ?: false ||
                                it.note?.toLowerCase(Locale.getDefault())?.contains(filterString) ?: false
                    }
                }
        }
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
        toBeSaved.clear()
        allNotes.value?.also {
            toBeSaved.addAll(it.filter { notesIds.contains(it.id) })
        }
        repository.deleteNotes(notesIds)
    }

    fun undeleteNotes() = viewModelScope.launch {
        toBeSaved.forEach {
            repository.insert(it)
        }
        toBeSaved.clear()
    }

    fun setNotesListFilter(newFilter: String) {
        filter.value = newFilter.toLowerCase(Locale.getDefault())
    }

}
