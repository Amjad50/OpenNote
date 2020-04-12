package com.amjad.opennote.data.entities

import android.graphics.Color
import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amjad.opennote.data.converters.DataConverters
import com.amjad.opennote.data.converters.NoteTypeConverters
import java.text.DateFormat
import java.util.*
import kotlin.text.Charsets.UTF_8

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

    fun getLastImage(): String {
        if (images.isNotEmpty())
            return images.substring(
                images.lastIndexOf(',', images.length - 2) + 1,
                images.length - 1
            )
        return ""
    }

    override fun toString(): String {
        return "Note(type=$type, title='$title', note='$note', date=$date, color=$color, id=$id)"
    }

    fun getSerializedStringArray(imagesCallback: (Array<String>) -> String): Array<String> {
        return arrayOf(
            id.toString(),
            title,
            encodeNote(note),
            DataConverters().dateToTimestamp(date).toString(),
            color.toString(),
            NoteTypeConverters().typeToTypeCode(type).toString(),
            imagesCallback(getAllImages())
        )
    }

    fun getAllImages(): Array<String> {
        if (images.isBlank())
            return arrayOf()
        val imagesList = images.split(",")

        return imagesList.subList(0, imagesList.size - 1).toTypedArray()
    }

    companion object {
        fun createNoteBasedOnType(type: NoteType): Note {
            return when (type) {
                NoteType.UNDEFINED_TYPE -> Note()
                NoteType.TEXT_NOTE -> Note()
                NoteType.CHECKABLE_LIST_NOTE -> CheckableListNote()
            }
        }

        fun encodeNote(note: String): String {
            return Base64.encodeToString(note.toByteArray(UTF_8), Base64.DEFAULT)
        }

        fun decodeNote(base64: String): String {
            return Base64.decode(base64, Base64.DEFAULT).toString(UTF_8)
        }

        fun serializedStringHeaderArray(): Array<String> {
            return arrayOf("id", "title", "note", "date", "color", "type", "images")
        }

        fun deserializeStringArray(array: Array<String>, imagesCallback: (String) -> String): Note {
            // FIXME: add private constructor to add all in one go
            return Note(
                id = array[0].toLong(),
                title = array[1],
                note = decodeNote(array[2]), date =
                DataConverters().fromTimestamp(array[3].toLong()),
                color = array[4].toInt()
            ).apply {
                type = NoteTypeConverters().fromTypeCode(array[5].toInt())
                images = imagesCallback(array[6])
            }
        }
    }
}

