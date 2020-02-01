package com.amjad.opennote.data.entities

import android.graphics.Color
import java.util.*

class CheckableListNote : Note {

    init {
        type = NoteType.CHECKABLE_LIST_NOTE
    }

    constructor(
        title: String = "",
        note: String = "",
        date: Date? = null,
        color: Int = Color.WHITE,
        id: Long = 0
    ) : super(title, note, date, color, id) {
        populateNoteList(note)
    }

    constructor(
        title: String = "",
        noteList: List<Item>,
        date: Date? = null,
        color: Int = Color.WHITE,
        id: Long = 0
    ) : super(title, "", date, color, id) {
        this.noteList.addAll(noteList)
        note = serializeNoteList()
    }

    val noteList = mutableListOf<Item>()

    override fun getNoteObject(): Note {
        updateNoteText()

        return this
    }

    private fun updateNoteText() {
        note = serializeNoteList()
    }

    private fun populateNoteList(serializedNote: String) {
        noteList.clear()
        if (serializedNote.isNotEmpty())
            noteList.addAll(serializedNote.split(SEPARATOR).map {
                Item(it.substring(1), it[0] == 'X')
            })
    }

    private fun serializeNoteList(): String {
        // put if its checked or not at the beginning and then proceed with the item text
        // then join all by the SEPARATOR string
        return noteList.joinToString(SEPARATOR) {
            (if (it.isChecked) "X" else "x") + it.text
        }
    }

    override fun toString(): String {
        return "CheckableListNote(type=$type, title='$title', noteList=$noteList, date=$date, color=$color, id=$id)"
    }

    companion object {
        // this is the BEL character, which is not printable
        // which means that no (normal) user can type it.
        // its hackable but the reason I don't care is that it's ok
        // this is not something big, if its hacked this means that the user can put
        // separators in the middle on notes which is not something very big of a deal
        private const val SEPARATOR = (7).toChar().toString()
    }

    // position property is only used by the recyclerView adapter
    // used -1, so that if it was not used properly it will throw an error
    data class Item(var text: String = "", var isChecked: Boolean = false, var position: Int = -1) :
        Comparable<Item> {
        override fun compareTo(other: Item): Int {
            return if (this.isChecked && !other.isChecked)
                1
            else if (!this.isChecked && other.isChecked)
                -1
            else 0
        }
    }
}