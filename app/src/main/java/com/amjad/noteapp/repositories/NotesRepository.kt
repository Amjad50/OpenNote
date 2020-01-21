package com.amjad.noteapp.repositories

import androidx.lifecycle.LiveData
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.data.NoteDao

class NotesRepository(private val noteDao: NoteDao) {
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note): Long {
        return noteDao.insert(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNotes(notesIds: List<Long>) {
        noteDao.deleteNotes(notesIds)
    }

    suspend fun updateNotesColor(notesIds: List<Long>, color: Int) {
        noteDao.updateNotesColor(notesIds, color)
    }
}