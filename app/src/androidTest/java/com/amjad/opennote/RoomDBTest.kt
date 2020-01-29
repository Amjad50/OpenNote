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
import org.junit.Assert.*
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

    @Test
    fun updateTasksListSingleNote() {
        var noteId = 0L
        runBlocking {
            noteId = noteDao.insert(CheckableListNote().getNoteObject())
        }

        val note = noteDao.getNote(noteId).blockingObserve()?.getNoteBasedOnType()

        log("Note first time $note")

        if (note !is CheckableListNote?) {
            assertTrue(false)
            return
        }

        note?.title = "hi"
        note?.noteList?.add(Pair("welcome", false))
        note?.noteList?.add(Pair("hello", true))
        note?.noteList?.add(Pair("hi again", false))
        note?.noteList?.add(Pair("thats cool", false))
        note?.noteList?.add(Pair("test", true))

        runBlocking {
            note?.also {
                noteDao.updateNote(it.getNoteObject())
            }
        }

        val noteSecondTime = noteDao.getNote(noteId).blockingObserve()?.getNoteBasedOnType()

        if (noteSecondTime !is CheckableListNote?) {
            assertTrue(false)
            return
        }

        log("Note second time (after update) $noteSecondTime")

        assertEquals("hi", noteSecondTime?.title)
        assertEquals(5, noteSecondTime?.noteList?.size)
        assertEquals("welcome", noteSecondTime?.noteList?.get(0)?.first)
        assertEquals("hello", noteSecondTime?.noteList?.get(1)?.first)
        assertEquals("thats cool", noteSecondTime?.noteList?.get(3)?.first)
        assertEquals("test", noteSecondTime?.noteList?.get(4)?.first)
        assertEquals("hi again", noteSecondTime?.noteList?.get(2)?.first)

        assertTrue(noteSecondTime?.noteList?.get(1)?.second!!)
        assertTrue(noteSecondTime?.noteList?.get(4)?.second!!)
        assertFalse(noteSecondTime?.noteList?.get(0)?.second!!)
        assertFalse(noteSecondTime?.noteList?.get(2)?.second!!)
        assertFalse(noteSecondTime?.noteList?.get(3)?.second!!)

        assertEquals(1L, noteSecondTime?.id)


        noteSecondTime?.noteList?.apply {
            this[0] = this[0].copy(first = "welcome changed")
            this[2] = this[2].copy(first = "hi again changed", second = true)
            this[3] = this[3].copy(second = true)
        }

        runBlocking {
            noteSecondTime?.also {
                noteDao.updateNote(it.getNoteObject())
            }
        }

        val noteThirdTime = noteDao.getNote(noteId).blockingObserve()?.getNoteBasedOnType()

        if (noteThirdTime !is CheckableListNote?) {
            assertTrue(false)
            return
        }

        log("Note third time time (after update) $noteThirdTime")

        assertEquals("hi", noteThirdTime?.title)
        assertEquals(5, noteThirdTime?.noteList?.size)
        assertEquals("welcome changed", noteThirdTime?.noteList?.get(0)?.first)
        assertEquals("hello", noteThirdTime?.noteList?.get(1)?.first)
        assertEquals("thats cool", noteThirdTime?.noteList?.get(3)?.first)
        assertEquals("test", noteThirdTime?.noteList?.get(4)?.first)
        assertEquals("hi again changed", noteThirdTime?.noteList?.get(2)?.first)

        assertFalse(noteThirdTime?.noteList?.get(0)?.second!!)
        assertTrue(noteThirdTime?.noteList?.get(1)?.second!!)
        assertTrue(noteThirdTime?.noteList?.get(2)?.second!!)
        assertTrue(noteThirdTime?.noteList?.get(3)?.second!!)
        assertTrue(noteThirdTime?.noteList?.get(4)?.second!!)

        assertEquals(1L, noteThirdTime?.id)

        noteThirdTime?.noteList?.apply {
            this[0] = this[0].copy(second = true)
            this[1] = this[1].copy(first = "number 1 changed")
            this[3] = this[3].copy(first = "number 3 changed also wow", second = false)
        }

        noteThirdTime?.noteList?.add(Pair("we also added another one???", false))
        noteThirdTime?.noteList?.add(Pair("one more", true))

        runBlocking {
            noteThirdTime?.also {
                noteDao.updateNote(it.getNoteObject())
            }
        }

        val noteForthTime = noteDao.getNote(noteId).blockingObserve()?.getNoteBasedOnType()

        if (noteForthTime !is CheckableListNote?) {
            assertTrue(false)
            return
        }

        log("Note forth time time (after update) $noteForthTime")

        assertEquals("hi", noteForthTime?.title)
        assertEquals(7, noteForthTime?.noteList?.size)
        assertEquals("welcome changed", noteForthTime?.noteList?.get(0)?.first)
        assertEquals("number 1 changed", noteForthTime?.noteList?.get(1)?.first)
        assertEquals("number 3 changed also wow", noteForthTime?.noteList?.get(3)?.first)
        assertEquals("test", noteForthTime?.noteList?.get(4)?.first)
        assertEquals("hi again changed", noteForthTime?.noteList?.get(2)?.first)
        assertEquals("one more", noteForthTime?.noteList?.get(6)?.first)
        assertEquals("we also added another one???", noteForthTime?.noteList?.get(5)?.first)

        assertTrue(noteForthTime?.noteList?.get(0)?.second!!)
        assertTrue(noteForthTime?.noteList?.get(1)?.second!!)
        assertTrue(noteForthTime?.noteList?.get(2)?.second!!)
        assertFalse(noteForthTime?.noteList?.get(3)?.second!!)
        assertTrue(noteForthTime?.noteList?.get(4)?.second!!)
        assertFalse(noteForthTime?.noteList?.get(5)?.second!!)
        assertTrue(noteForthTime?.noteList?.get(6)?.second!!)

        assertEquals(1L, noteForthTime?.id)
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