package com.amjad.opennote.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.amjad.opennote.data.Note
import com.amjad.opennote.data.NoteDatabase
import com.amjad.opennote.repositories.NotesRepository
import com.amjad.opennote.ui.adapters.NoteListSelector
import kotlinx.coroutines.launch
import java.util.*

class NoteListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotesRepository

    private val allNotes: LiveData<List<Note>>
    private val toBeSaved = mutableListOf<Note>()
    private val filter = MutableLiveData<String>("")

    val selector = NoteListSelector<Long>()
    val filteredAllNotes: LiveData<List<Note>>


    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()

        repository = NotesRepository(wordsDao)
        allNotes = repository.allNotes

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

    fun deleteNotes(notesIds: List<Long>) = viewModelScope.launch {
        toBeSaved.clear()
        allNotes.value?.also { notes ->
            toBeSaved.addAll(notes.filter { note -> notesIds.contains(note.id) })
        }
        repository.deleteNotes(notesIds)
    }

    fun unDeleteNotes() = viewModelScope.launch {
        toBeSaved.forEach {
            repository.insert(it)
        }
        toBeSaved.clear()
    }

    fun setNotesListFilter(newFilter: String) {
        filter.value = newFilter.toLowerCase(Locale.getDefault())
    }

    fun getNotesListFilter(): String =
        filter.value ?: ""

    fun updateNotesColor(notesIds: List<Long>, color: Int) = viewModelScope.launch {
        repository.updateNotesColor(notesIds, color)
    }

}
