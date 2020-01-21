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
    private val selectedNoteID = MutableLiveData<Long>()
    private val toBeSaved = mutableListOf<Note>()
    private val filter = MutableLiveData<String>("")

    val filteredAllNotes: LiveData<List<Note>>
    val currentNote: LiveData<Note>


    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()

        repository = NotesRepository(wordsDao)
        allNotes = repository.allNotes
        currentNote = Transformations.switchMap(selectedNoteID) { id ->
            Transformations.map(allNotes) { notes ->
                notes.find { note -> note.id == id } ?: Note()
            }
        }

        filteredAllNotes = Transformations.switchMap(filter) { filterString ->
            if (filterString.isNullOrEmpty())
                allNotes
            else
                Transformations.map(allNotes) { notes ->
                    notes.filter { note ->
                        note.title.toLowerCase(Locale.getDefault()).contains(filterString) ||
                                note.note.toLowerCase(Locale.getDefault()).contains(filterString)
                    }
                }
        }
    }

    fun setNoteID(id: Long) {
        selectedNoteID.value = id
    }

    fun getSelectedNoteID(): Long = selectedNoteID.value ?: -1L

    fun insertCurrentNote() = viewModelScope.launch {
        currentNote.value?.let {
            // update the time of inserting
            it.date = Date()

            if (!(it.title.isEmpty() && it.note.isEmpty()))
                setNoteID(repository.insert(it))
        }
    }

    fun updateCurrentNote() = viewModelScope.launch {
        currentNote.value?.also {
            it.date = Date()
            repository.updateNote(it)
        }
    }

    fun deleteNotes(notesIds: List<Long>) = viewModelScope.launch {
        toBeSaved.clear()
        allNotes.value?.also { notes ->
            toBeSaved.addAll(notes.filter { note -> notesIds.contains(note.id) })
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

    fun updateNotesColor(notesIds: List<Long>, color: Int) = viewModelScope.launch {
        repository.updateNotesColor(notesIds, color)
    }

}
