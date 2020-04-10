package com.amjad.opennote.data.databases

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amjad.opennote.data.converters.DataConverters
import com.amjad.opennote.data.converters.NoteTypeConverters
import com.amjad.opennote.data.daos.NoteDao
import com.amjad.opennote.data.entities.Note
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Database(entities = [Note::class], version = 4, exportSchema = false)
@TypeConverters(DataConverters::class, NoteTypeConverters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    // FIXME: change to concurrent or async code
    fun saveDatabase(outstream: OutputStream) {
        val noteslivedata = noteDao().getAllNotes()

        val latch = CountDownLatch(1)

        val observer = object : Observer<List<Note>> {
            override fun onChanged(notes: List<Note>?) {
                val csv_writer = CSVWriter(outstream.writer())
                csv_writer.writeNext(Note.serializedStringHeaderArray())
                val serializedNotes = notes?.map { note ->
                    note.getSerializedStringArray()
                }

                csv_writer.writeAll(serializedNotes)
                csv_writer.close()

                // remove the observer as we are done here
                noteslivedata.removeObserver(this)
                latch.countDown()
            }
        }

        noteslivedata.observeForever(observer)
        latch.await(5, TimeUnit.SECONDS)
    }

    // FIXME: change to concurrent or async code
    fun restoreDatabase(instream: InputStream) {
        val csv_reader = CSVReader(instream.reader())
        // Ignore the header
        csv_reader.readNext()

        val allNotes = csv_reader.readAll().map {
            Note.deserializeStringArray(it)
        }

        csv_reader.close()

        runBlocking {
            allNotes.forEach {
                noteDao().insert(it)
            }
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note_table RENAME TO _old_note_table")

                // default value of color is -1 which is equal to Color.WHITE
                database.execSQL(
                    "CREATE TABLE note_table(" +
                            "id INTEGER NOT NULL PRIMARY KEY," +
                            "title TEXT NOT NULL," +
                            "note TEXT NOT NULL," +
                            "date INTEGER," +
                            "color INTEGER NOT NULL DEFAULT -1)"
                )
                database.execSQL(
                    "INSERT INTO note_table (id, title, note, date)" +
                            "SELECT id, title, note, date FROM _old_note_table"
                )
                database.execSQL("DROP TABLE _old_note_table")
                //database.execSQL("ALTER TABLE note_table ADD COLUMN color INTEGER NOT NULL DEFAULT -1")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note_table ADD COLUMN type INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note_table ADD COLUMN images TEXT NOT NULL DEFAULT \"\"")
            }
        }

        fun getDatabase(context: Context): NoteDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "word_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}