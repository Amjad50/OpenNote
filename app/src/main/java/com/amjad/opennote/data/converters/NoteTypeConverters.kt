package com.amjad.opennote.data.converters

import androidx.room.TypeConverter
import com.amjad.opennote.data.entities.NoteType

class NoteTypeConverters {
    @TypeConverter
    fun fromTypeCode(typeCode: Int): NoteType {
        return when (typeCode) {
            0 -> NoteType.TEXT_NOTE
            1 -> NoteType.CHECKABLE_LIST_NOTE
            else -> NoteType.UNDEFINED_TYPE
        }
    }

    @TypeConverter
    fun typeToTypeCode(type: NoteType): Int {
        return type.typeCode
    }
}