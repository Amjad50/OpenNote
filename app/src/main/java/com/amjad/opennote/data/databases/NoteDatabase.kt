package com.amjad.opennote.data.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.util.Base64
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
import com.amjad.opennote.data.entities.NoteType
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

@Database(entities = [Note::class], version = 6, exportSchema = false)
@TypeConverters(DataConverters::class, NoteTypeConverters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    // TODO: move base64 conversion to somewhere else, maybe Note class
    private suspend fun imageToBase64(file: File): String {
        return withContext(Dispatchers.IO) {
            val instream = FileInputStream(file)
            val data = instream.use {
                it.readBytes()
            }
            // NO_WRAP is super important when reading it later, when using wrapping
            // with large files, like images it will take a very long time reading the
            // base64 string. And also this way, less bytes as we remove newline bytes.
            Base64.encodeToString(data, Base64.NO_WRAP)
        }
    }

    private suspend fun base64ToImage(base64: String, file: File) {
        return withContext(Dispatchers.IO) {
            val outstream = FileOutputStream(file)
            outstream.use {
                it.write(Base64.decode(base64, Base64.NO_WRAP))
            }
        }
    }

    suspend fun saveDatabase(context: Context, outstream: OutputStream) {
        withContext(Dispatchers.IO) {
            val notes = noteDao().getAllNotesAsync()
            val imagesFile = File(context.filesDir, "images")

            val csv_writer = CSVWriter(outstream.writer())
            csv_writer.writeNext(Note.serializedStringHeaderArray())
            notes.forEach { note ->
                val serializedNote = note.getSerializedStringArray { images ->
                    val imagesBase64 = images.map { image ->
                        runBlocking {
                            imageToBase64(File(imagesFile, "$image.png"))
                        }
                    }
                    imagesBase64.joinToString("|")
                }

                csv_writer.writeNext(serializedNote)
            }

            csv_writer.close()
        }
    }

    suspend fun restoreDatabase(context: Context, instream: InputStream) {
        withContext(Dispatchers.IO) {
            val csv_reader = CSVReader(instream.reader())
            val imagesFile = File(context.filesDir, "images")

            imagesFile.mkdir()

            // Ignore the header
            csv_reader.readNext()

            while (true) {
                val serializedNote = csv_reader.readNext()
                if (serializedNote == null)
                    break

                val note = Note.deserializeStringArray(serializedNote) { images ->
                    val uuids = images.split("|").map { base64 ->

                        val uuid = UUID.randomUUID().toString()

                        val savedImage = File(imagesFile, "$uuid.png")
                        runBlocking {
                            base64ToImage(base64, savedImage)
                        }
                        uuid
                    }

                    uuids.joinToString(separator = ",", postfix = ",")
                }

                noteDao().insert(note)
            }

            csv_reader.close()
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // increment the id by one because id 0 is the zero node
                database.execSQL("UPDATE note_table SET id = -id - 1")
                database.execSQL("UPDATE note_table SET id = -id")
                database.insert(
                    "note_table",
                    SQLiteDatabase.CONFLICT_ABORT,
                    DEFAULT_NOTE_FOLDER_CONTENTVALUES
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // remove the content of the note on the root folder
                database.execSQL("UPDATE note_table SET note = \"\" WHERE id = 0")
                database.execSQL("ALTER TABLE note_table ADD COLUMN parentId INT NOT NULL DEFAULT 0")
                // the root does not have parent
                database.execSQL("UPDATE note_table SET parentId = -1 WHERE id = 0")
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
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.insert(
                                "note_table",
                                SQLiteDatabase.CONFLICT_ABORT,
                                DEFAULT_NOTE_FOLDER_CONTENTVALUES
                            )
                        }
                    })
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private val DEFAULT_NOTE_FOLDER_CONTENTVALUES = ContentValues(7).apply {
            put("id", 0)
            put("title", "")
            put("note", "")
            put("date", DataConverters().dateToTimestamp(Date()))
            put("color", Color.WHITE)
            put("type", NoteTypeConverters().typeToTypeCode(NoteType.FOLDER_NOTE))
            put("images", "")
        }
    }
}