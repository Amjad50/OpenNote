package com.amjad.opennote.ui.viewmodels

import android.app.Application
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import com.amjad.opennote.data.databases.NoteDatabase
import com.amjad.opennote.data.entities.Note
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.repositories.NotesRepository
import com.bumptech.glide.Glide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
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

    var selectNextListItem = false


    init {
        val wordsDao = NoteDatabase.getDatabase(application).noteDao()

        repository = NotesRepository(wordsDao)
        note = Transformations.switchMap(selectedNoteID) { id ->
            Transformations.map(repository.getNote(id)) {
                it.getNoteBasedOnType()
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

    // FIXME: detected a weird bug crashes here, something about cannot find apk file??
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

    fun addImage(uuid: String) {
        note.value?.addImage(uuid)
        // TODO: when implementing images view, check which is better, to update DB or just notify
        notifyNoteUpdated()
    }

    class StatusAndActionBarStyles {
        var saved: Boolean = false
        var background: Int = 0
        var elevation: Float = 0f
        var statusBarColor: Int = 0
    }

    companion object {
        @JvmStatic
        @BindingAdapter("noteImage")
        fun loadImage(view: ImageView, uuid: String?) {
            val image = File(view.context.filesDir, "images/$uuid.png")
            Glide.with(view.context)
                .load(image)
                .into(view);
        }
    }
}
