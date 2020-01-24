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
import com.amjad.opennote.data.Note
import com.amjad.opennote.data.NoteDao
import com.amjad.opennote.data.NoteDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
    @Throws(Exception::class)
    fun addNewNoteTest() {

        runBlocking {
            noteDao.insert(TESTING_NOTE)
        }


        val notes = noteDao.getAllNotes()

        val note = notes.blockingObserve()?.get(0)


        Log.d("BB", "$note")

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

    companion object {
        val TESTING_NOTE = Note(
            id = 0,
            title = "welcome",
            note = "hello",
            date = Date(1321654),
            color = Color.BLUE
        )
    }
}