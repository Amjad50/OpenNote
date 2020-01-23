package com.amjad.opennote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Note::class], version = 2, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

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

        fun getDatabase(context: Context): NoteDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "word_database"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}