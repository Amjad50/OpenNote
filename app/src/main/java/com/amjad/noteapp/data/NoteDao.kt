package com.amjad.noteapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Update
    suspend fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note): Long

    @Query("DELETE FROM note_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM note_table WHERE id in (:notesIds)")
    suspend fun deleteNotes(notesIds: List<Long>)

    @Query("UPDATE note_table SET color = :color WHERE id in (:notesIds)")
    suspend fun updateNotesColor(notesIds: List<Long>, color: Int)
}