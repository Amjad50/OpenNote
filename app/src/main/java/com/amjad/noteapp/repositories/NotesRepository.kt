package com.amjad.noteapp.repositories

import androidx.lifecycle.LiveData
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.data.NoteDao

class NotesRepository(private val noteDao: NoteDao) {
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    fun getNote(id: Int): LiveData<Note> {
        return noteDao.getNote(id)
    }
}