package com.amjad.opennote.data.entities

import android.graphics.Color
import java.util.*

class FolderNote(title: String = "", date: Date? = null, color: Int = Color.WHITE, id: Long = 0) :
    Note(title, "", date, color, id) {

    init {
        type = NoteType.FOLDER_NOTE
    }

    override fun getNoteObject(): Note {
        // make sure its empty all the time
        note = ""
        return this
    }

    override fun toString(): String {
        return "FolderNote(type=$type, title='$title', date=$date, color=$color, id=$id)"
    }
}