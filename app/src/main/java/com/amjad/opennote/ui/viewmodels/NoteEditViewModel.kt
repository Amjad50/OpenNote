package com.amjad.opennote.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.amjad.opennote.data.databases.NoteDatabase
import com.amjad.opennote.data.entities.Note
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.repositories.NotesRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class NoteEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotesRepository

    private val selectedNoteID = MutableLiveData<Long>()

    val oldStatusAndActionBarStyles = StatusAndActionBarStyles()

    val note: LiveData<Note>

    var isNoteSelected = false
        private set(v) {
            field = v
        }


    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()

        repository = NotesRepository(wordsDao)
        note = Transformations.switchMap(selectedNoteID) { id ->
            Transformations.map(repository.getNote(id)) {
                it.getNoteBasedOnType()
            }
        }
    }

    /**
     * @return true if the selection has actually changes, else false
     */
    fun setNoteID(id: Long): Boolean {
        if (!isNoteSelected) {
            selectedNoteID.value = id
            isNoteSelected = true
            return true
        }
        return false
    }

    fun insertNewNote(type: NoteType) = viewModelScope.launch {
        setNoteID(repository.insert(Note.createNoteBasedOnType(type).apply { date = Date() }))
    }

    fun updateCurrentNote() = GlobalScope.launch {
        note.value?.also {
            val note = it.getNoteObject()
            // TODO: need condition to update the date, or from the view
            note.date = Date()

            if (!(note.title.isEmpty() && note.note.isEmpty()))
                repository.updateNote(note)
            else
                repository.deleteNote(note)
        }
    }

    override fun onCleared() {
        updateCurrentNote()
    }

    class StatusAndActionBarStyles {
        var saved: Boolean = false
        var background: Int = 0
        var elevation: Float = 0f
        var statusBarColor: Int = 0
    }
}
