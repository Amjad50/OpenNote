package com.amjad.opennote.ui.viewmodels

import android.app.Application
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class NoteEditViewModel(application: Application) : BaseNoteViewModel(application) {

    var selectNextListItem = false

    private fun updateCurrentNote() = GlobalScope.launch {
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

    companion object {
        @JvmStatic
        @BindingAdapter("noteImage")
        fun loadImage(view: ImageView, uuid: String?) {
            if (!uuid.isNullOrBlank()) {
                val image = File(view.context.filesDir, "images/$uuid.png")
                Glide.with(view.context)
                    .load(image)
                    .into(view)
            }
        }
    }
}
