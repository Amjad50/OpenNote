package com.amjad.opennote.data.entities

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import java.util.*

@Entity(tableName = "note_table")
open class Note(
    var title: String = "",
    var note: String = "",
    var date: Date? = null,
    var color: Int = Color.WHITE,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
) {
    // FIXME: find a way to make this uneditable by users only room
    var images: String = ""
    var type: NoteType = NoteType.TEXT_NOTE

    fun getFormattedDate(): String {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
        return date?.let { dateFormat.format(it) } ?: ""
    }

    open fun getNoteObject(): Note {
        return this
    }

    fun getCheckableListNote(): CheckableListNote {
        if (type != NoteType.CHECKABLE_LIST_NOTE)
            throw IllegalArgumentException("To get CheckableListNote note.type must be CHECKABLE_LIST_NOTE")

        return CheckableListNote(
            title,
            note,
            date,
            color,
            id
        ).apply { images = this@Note.images }
    }

    /**
     * @return the subclassed object based on the value of type
     */
    fun getNoteBasedOnType(): Note {
        return if (type == NoteType.CHECKABLE_LIST_NOTE)
            getCheckableListNote()
        else
            this
    }

    fun addImage(uuid: String) {
        images += "$uuid,"
    }

    fun removeImage(uuid: String) {
        val index = images.indexOf(uuid)
        if (index != -1)
            images = images.removeRange(index, index + uuid.length + 1)
    }

    override fun toString(): String {
        return "Note(type=$type, title='$title', note='$note', date=$date, color=$color, id=$id)"
    }

    companion object {
        fun createNoteBasedOnType(type: NoteType): Note {
            return when (type) {
                NoteType.UNDEFINED_TYPE -> Note()
                NoteType.TEXT_NOTE -> Note()
                NoteType.CHECKABLE_LIST_NOTE -> CheckableListNote()
            }
        }
    }
}

