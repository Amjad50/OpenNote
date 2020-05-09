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

abstract class BaseNoteViewModel(application: Application) : AndroidViewModel(application) {

    protected val database: NoteDatabase
    protected val repository: NotesRepository

    protected val selectedNoteID = MutableLiveData<Long>()
    val note: LiveData<Note>

    var isNoteSelected: Boolean = false
        protected set(v) {
            field = v
        }

    val oldStatusAndActionBarStyles = StatusAndActionBarStyles()

    init {
        database = NoteDatabase.getDatabase(application)
        val wordsDao = database.noteDao()

        repository = NotesRepository(wordsDao)

        note = Transformations.switchMap(selectedNoteID) { id ->
            Transformations.map(repository.getNote(id)) { note ->
                note.getNoteBasedOnType()
            }
        }
    }

    fun notifyNoteUpdated() {
        (note as MutableLiveData).run {
            value = value
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

    fun getNoteId(): Long = selectedNoteID.value ?: -1L

    // FIXME: detected a weird bug crashes here, something about cannot find apk file??
    fun insertNewNote(type: NoteType, parentId: Long) = viewModelScope.launch {
        setNoteID(repository.insert(Note.createNoteBasedOnType(type).apply {
            date = Date()
            this.parentId = parentId
        }))
    }

    class StatusAndActionBarStyles {
        var saved: Boolean = false
        var background: Int = 0
        var elevation: Float = 0f
        var statusBarColor: Int = 0
    }

    private fun updateCurrentNote() = GlobalScope.launch {
        note.value?.also {
            val note = it.getNoteObject()
            // TODO: need condition to update the date, or from the view
            note.date = Date()

            // only delete empty notes that are not folders
            if (note.title.isNotEmpty() || note.note.isNotEmpty() || note.type == NoteType.FOLDER_NOTE)
                repository.updateNote(note)
            else
                repository.deleteNote(note)
        }
    }

    override fun onCleared() {
        updateCurrentNote()
    }
}