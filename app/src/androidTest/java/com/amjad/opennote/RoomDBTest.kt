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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.text.Charsets.UTF_8

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
                TESTING_LIST_NOTE.noteList.map { it.text },
                note?.noteList?.map { it.text })
            assertEquals(
                TESTING_LIST_NOTE.noteList.map { it.isChecked },
                note?.noteList?.map { it.isChecked })
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
                TESTING_LIST_NOTE.noteList.map { it.text },
                taskNote?.noteList?.map { it.text })
            assertEquals(
                TESTING_LIST_NOTE.noteList.map { it.isChecked },
                taskNote?.noteList?.map { it.isChecked })
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
        note?.noteList?.add(CheckableListNote.Item("welcome", false))
        note?.noteList?.add(CheckableListNote.Item("hello", true))
        note?.noteList?.add(CheckableListNote.Item("hi again", false))
        note?.noteList?.add(CheckableListNote.Item("thats cool", false))
        note?.noteList?.add(CheckableListNote.Item("test", true))

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
        assertEquals("welcome", noteSecondTime?.noteList?.get(0)?.text)
        assertEquals("hello", noteSecondTime?.noteList?.get(1)?.text)
        assertEquals("thats cool", noteSecondTime?.noteList?.get(3)?.text)
        assertEquals("test", noteSecondTime?.noteList?.get(4)?.text)
        assertEquals("hi again", noteSecondTime?.noteList?.get(2)?.text)

        assertTrue(noteSecondTime?.noteList?.get(1)?.isChecked!!)
        assertTrue(noteSecondTime?.noteList?.get(4)?.isChecked!!)
        assertFalse(noteSecondTime?.noteList?.get(0)?.isChecked!!)
        assertFalse(noteSecondTime?.noteList?.get(2)?.isChecked!!)
        assertFalse(noteSecondTime?.noteList?.get(3)?.isChecked!!)

        assertEquals(1L, noteSecondTime?.id)


        noteSecondTime?.noteList?.apply {
            this[0].text = "welcome changed"
            this[2].text = "hi again changed"
            this[2].isChecked = true
            this[3].isChecked = true
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
        assertEquals("welcome changed", noteThirdTime?.noteList?.get(0)?.text)
        assertEquals("hello", noteThirdTime?.noteList?.get(1)?.text)
        assertEquals("thats cool", noteThirdTime?.noteList?.get(3)?.text)
        assertEquals("test", noteThirdTime?.noteList?.get(4)?.text)
        assertEquals("hi again changed", noteThirdTime?.noteList?.get(2)?.text)

        assertFalse(noteThirdTime?.noteList?.get(0)?.isChecked!!)
        assertTrue(noteThirdTime?.noteList?.get(1)?.isChecked!!)
        assertTrue(noteThirdTime?.noteList?.get(2)?.isChecked!!)
        assertTrue(noteThirdTime?.noteList?.get(3)?.isChecked!!)
        assertTrue(noteThirdTime?.noteList?.get(4)?.isChecked!!)

        assertEquals(1L, noteThirdTime?.id)

        noteThirdTime?.noteList?.apply {
            this[0].isChecked = true
            this[1].text = "number 1 changed"
            this[3].text = "number 3 changed also wow"
            this[3].isChecked = false
        }

        noteThirdTime?.noteList?.add(CheckableListNote.Item("we also added another one???", false))
        noteThirdTime?.noteList?.add(CheckableListNote.Item("one more", true))

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
        assertEquals("welcome changed", noteForthTime?.noteList?.get(0)?.text)
        assertEquals("number 1 changed", noteForthTime?.noteList?.get(1)?.text)
        assertEquals("number 3 changed also wow", noteForthTime?.noteList?.get(3)?.text)
        assertEquals("test", noteForthTime?.noteList?.get(4)?.text)
        assertEquals("hi again changed", noteForthTime?.noteList?.get(2)?.text)
        assertEquals("one more", noteForthTime?.noteList?.get(6)?.text)
        assertEquals("we also added another one???", noteForthTime?.noteList?.get(5)?.text)

        assertTrue(noteForthTime?.noteList?.get(0)?.isChecked!!)
        assertTrue(noteForthTime?.noteList?.get(1)?.isChecked!!)
        assertTrue(noteForthTime?.noteList?.get(2)?.isChecked!!)
        assertFalse(noteForthTime?.noteList?.get(3)?.isChecked!!)
        assertTrue(noteForthTime?.noteList?.get(4)?.isChecked!!)
        assertFalse(noteForthTime?.noteList?.get(5)?.isChecked!!)
        assertTrue(noteForthTime?.noteList?.get(6)?.isChecked!!)

        assertEquals(1L, noteForthTime?.id)
    }

    @Test
    fun serializeNoteDBTest() {
        runBlocking {
            noteDao.insert(TESTING_NOTE.getNoteObject())
            noteDao.insert(TESTING_LIST_NOTE.getNoteObject())
        }

        val outstream = ByteArrayOutputStream(256)

        database.saveDatabase(outstream)
        log(outstream.toByteArray().toString(UTF_8))

        runBlocking {
            noteDao.deleteAll()
        }

        val instream = ByteArrayInputStream(outstream.toByteArray())

        outstream.close()

        database.restoreDatabase(instream)

        instream.close()

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
                TESTING_LIST_NOTE.noteList.map { it.text },
                taskNote?.noteList?.map { it.text })
            assertEquals(
                TESTING_LIST_NOTE.noteList.map { it.isChecked },
                taskNote?.noteList?.map { it.isChecked })
        }


        // clear
        runBlocking {
            noteDao.deleteAll()
        }
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
                CheckableListNote.Item("welcome", true),
                CheckableListNote.Item("another welcome", true),
                CheckableListNote.Item("test note checkable", true),
                CheckableListNote.Item("yes", false),
                CheckableListNote.Item("should work", true)
            )
        )
    }
}