package com.amjad.opennote.ui.viewmodels

import android.app.Application
import android.content.Context
import java.io.File

class NoteEditViewModel(application: Application) : BaseNoteViewModel(application) {

    var selectNextListItem = false

    fun addImage(uuid: String) {
        note.value?.addImage(uuid)
        // TODO: when implementing images view, check which is better, to update DB or just notify
        notifyNoteUpdated()
    }

    fun deleteLastImage(context: Context) {
        note.value?.apply {
            val uuid = getLastImage()
            if (!uuid.isBlank()) {
                // delete from the database storage
                removeImage(uuid)

                // delete from the device
                val imageFile = File(context.filesDir, "images/$uuid.png")
                imageFile.delete()
            }
        }
    }
}
