package com.amjad.opennote.data.entities

import android.graphics.Color
import java.util.*

class CheckableListNote internal constructor(
    type: NoteType,
    title: String = "",
    note: String = "",
    date: Date? = null,
    color: Int = Color.WHITE,
    id: Long = 0
) : Note(type, title, note, date, color, id) {

    init {
        populateNoteList(note)
    }

    val noteList = mutableListOf<Pair<String, Boolean>>()

    override fun getNoteObject(): Note {
        updateNoteText()

        return this
    }

    private fun updateNoteText() {
        note = serializeNoteList()
    }

    private fun populateNoteList(serializedNote: String) {
        throw NotImplementedError("CheckableListNote::populateNoteList, which is used to convert string note to list")
    }

    private fun serializeNoteList(): String {
        var finalResult = ""

        throw NotImplementedError("CheckableListNote::serializeNoteList, which is used to convert the list to string note")
    }
}