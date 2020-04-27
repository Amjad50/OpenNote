package com.amjad.opennote.data.entities

enum class NoteType(val typeCode: Int) {
    UNDEFINED_TYPE(-1),
    TEXT_NOTE(0),
    CHECKABLE_LIST_NOTE(1),
    FOLDER_NOTE(2)
}
