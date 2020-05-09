package com.amjad.opennote.ui.viewmodels

import android.app.Application
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.io.File

class NoteEditViewModel(application: Application) : BaseNoteViewModel(application) {

    var selectNextListItem = false

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
