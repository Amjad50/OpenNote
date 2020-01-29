package com.amjad.opennote

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amjad.opennote.data.daos.NoteDao
import com.amjad.opennote.data.databases.NoteDatabase
import com.amjad.opennote.data.entities.CheckableListNote
import com.amjad.opennote.data.entities.Note
import com.amjad.opennote.data.entities.NoteType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RoomDBTest {


    // make the test run in Main thread to fix the problem of observeForever need to be in the
    // main thread
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NoteDatabase
    private lateinit var noteDao: NoteDao

    @Before
    @Throws(Exception::class)
    fun initDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, NoteDatabase::class.java
        )
            .build()

        noteDao = database.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    // extension function
    private fun <T> LiveData<T>.blockingObserve(): T? {
        var value: T? = null
        val latch = CountDownLatch(1)

        val observer = Observer<T> { t ->
            value = t
            latch.countDown()
        }

        observeForever(observer)

        latch.await(3, TimeUnit.SECONDS)
        return value
    }

    @Test
    fun addNewNoteTest() {
        runBlocking {
            noteDao.insert(TESTING_NOTE.getNoteObject())
        }


        val notes = noteDao.getAllNotes()

        val note = notes.blockingObserve()?.get(0)


        log("$note")

        assertEquals(TESTING_NOTE.date, note?.date)
        assertEquals(TESTING_NOTE.title, note?.title)
        assertEquals(TESTING_NOTE.note, note?.note)
        assertEquals(TESTING_NOTE.color, note?.color)

        // as its the first note to be inserted
        assertEquals(1L, note?.id)

        // clear
        runBlocking {
            noteDao.deleteAll()
        }
    }

    @Test
    fun addNewTasksListNote() {
        runBlocking {
            noteDao.insert(TESTING_LIST_NOTE.getNoteObject())
        }

        val notes = noteDao.getAllNotes()

        val note = notes.blockingObserve()?.get(0)?.getNoteBasedOnType()

        log("$note")

        assertEquals(note?.type, NoteType.CHECKABLE_LIST_NOTE)
        if (note !is CheckableListNote?) {
            // this is used to enable smart cast below for (taskNote?.noteList)
            assertTrue(false)
        } else {
            assertEquals(
                TESTING_LIST_NOTE.date,
                note?.date
            )
            assertEquals(TESTING_LIST_NOTE.title, note?.title)
            assertEquals(TESTING_LIST_NOTE.color, note?.color)
            assertEquals(
                TESTING_LIST_NOTE.noteList.map { it.first },
                note?.noteList?.map { it.first })
            assertEquals(
                TESTING_LIST_NOTE.noteList.map { it.second },
                note?.noteList?.map { it.second })
        }

        // as its the first note to be inserted
        assertEquals(1L, note?.id)

        // clear
        runBlocking {
            noteDao.deleteAll()
        }
    }

    @Test
    fun addNewTextAndTasksNotes() {
        runBlocking {
            noteDao.insert(TESTING_NOTE.getNoteObject())
            noteDao.insert(TESTING_LIST_NOTE.getNoteObject())
        }

        val notes = noteDao.getAllNotes().blockingObserve()

        log("$notes")

        assertEquals(2, notes?.size)
        val textNote =
            (if (notes?.get(0)?.type == NoteType.TEXT_NOTE) notes[0] else notes?.get(1))?.getNoteBasedOnType()
        val taskNote =
            (if (notes?.get(0)?.type == NoteType.CHECKABLE_LIST_NOTE) notes[0] else notes?.get(1))?.getNoteBasedOnType()

        log("$textNote")
        log("$taskNote")

        assertEquals(TESTING_NOTE.date, textNote?.date)
        assertEquals(TESTING_NOTE.title, textNote?.title)
        assertEquals(TESTING_NOTE.note, textNote?.note)
        assertEquals(TESTING_NOTE.color, textNote?.color)

        if (taskNote !is CheckableListNote?) {
            // this is used to enable smart cast below for (taskNote?.noteList)
            assertTrue(false)
        } else {
            assertEquals(TESTING_LIST_NOTE.title, taskNote?.title)
            assertEquals(TESTING_LIST_NOTE.color, taskNote?.color)
            assertEquals(
                TESTING_LIST_NOTE.noteList.map { it.first },
                taskNote?.noteList?.map { it.first })
            assertEquals(
                TESTING_LIST_NOTE.noteList.map { it.second },
                taskNote?.noteList?.map { it.second })
        }


        // clear
        runBlocking {
            noteDao.deleteAll()
        }
    }

    @Test
    fun updateTextNote() {
        var noteId = 0L
        runBlocking {
            noteId = noteDao.insert(Note().getNoteObject())
        }

        val note = noteDao.getNote(noteId).blockingObserve()?.getNoteBasedOnType()

        log("Note first time $note")

        assertEquals(NoteType.TEXT_NOTE, note?.type)

        note?.note = "welcome"
        note?.title = "hi"

        runBlocking {
            note?.also {
                noteDao.updateNote(it.getNoteObject())
            }
        }

        val noteSecondTime = noteDao.getNote(noteId).blockingObserve()?.getNoteBasedOnType()

        assertEquals(NoteType.TEXT_NOTE, noteSecondTime?.type)

        log("Note second time (after update) $noteSecondTime")

        assertEquals("welcome", noteSecondTime?.note)
        assertEquals("hi", noteSecondTime?.title)

        assertEquals(1L, noteSecondTime?.id)
    }

    companion object {
        private const val LOG_TAG = "TESTING"

        fun log(message: String) {
            Log.d(LOG_TAG, message)
        }

        val TESTING_NOTE = Note(
            id = 0,
            title = "welcome",
            note = "hello",
            date = Date(1321654),
            color = Color.BLUE
        )

        val TESTING_LIST_NOTE = CheckableListNote(
            id = 0,
            title = "goals",
            date = Date(321),
            noteList = listOf(
                Pair("welcome", true),
                Pair("another welcome", true),
                Pair("test note checkable", true),
                Pair("yes", false),
                Pair("should work", true)
            )
        )
    }
}