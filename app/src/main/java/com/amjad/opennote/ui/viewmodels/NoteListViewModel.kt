package com.amjad.opennote.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.amjad.opennote.data.databases.NoteDatabase
import com.amjad.opennote.data.entities.Note
import com.amjad.opennote.repositories.NotesRepository
import com.amjad.opennote.ui.adapters.NoteListSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class NoteListViewModel(application: Application) : AndroidViewModel(application) {

    private val database: NoteDatabase
    private val repository: NotesRepository

    private val allNotes: LiveData<List<Note>>
    private val toBeSaved = mutableListOf<Note>()
    private val filter = MutableLiveData<String>("")

    val selector = NoteListSelector<Long>()
    val filteredAllNotes: LiveData<List<Note>>


    init {
        database = NoteDatabase.getDatabase(application)
        val wordsDao = database.noteDao()

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

    fun backupDatabase(context: Context, outputStream: OutputStream, callback: () -> Unit) =
        GlobalScope.launch {
            val result = async(Dispatchers.Main) {
                database.saveDatabase(context, outputStream)
            }
            result.invokeOnCompletion {
                callback()
            }
        }

    fun restoreDatabase(context: Context, inputStream: InputStream, callback: () -> Unit) =
        GlobalScope.launch {
            val result = async(Dispatchers.Main) {
                database.restoreDatabase(context, inputStream)
            }
            result.invokeOnCompletion {
                callback()
            }
        }

    fun deleteAll() = GlobalScope.launch {
        repository.deleteAll()
    }
}
