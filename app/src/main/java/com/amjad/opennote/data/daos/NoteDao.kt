package com.amjad.opennote.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.amjad.opennote.data.entities.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table ORDER BY id DESC")
    suspend fun getAllNotesAsync(): List<Note>

    @Query("SELECT * FROM note_table WHERE id = :id")
    fun getNote(id: Long): LiveData<Note>

    @Query("SELECT * FROM note_table WHERE parentId = :parentId ORDER BY id DESC")
    fun getChildrenNotes(parentId: Long): LiveData<List<Note>>

    @Update
    suspend fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note): Long

    // do not delete 0 as its the root
    @Query("DELETE FROM note_table where id != 0")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM note_table WHERE id in (:notesIds) and id != 0")
    suspend fun deleteNotes(notesIds: List<Long>)

    @Query("UPDATE note_table SET color = :color WHERE id in (:notesIds)")
    suspend fun updateNotesColor(notesIds: List<Long>, color: Int)
}