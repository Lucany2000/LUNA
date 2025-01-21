package com.luna.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor
import android.util.Log

open class MainDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "Music.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {

        createTable(db, "SongList")
        createTable(db, "Blacklist")

//        val SQL_CREATE_SONGLIST = """
//            CREATE TABLE IF NOT EXISTS SongList (
//                hash TEXT PRIMARY KEY,
//                title TEXT NOT NULL,
//                artist TEXT NOT NULL,
//                album TEXT NOT NULL,
//                albumartist TEXT NOT NULL,
//                song TEXT NOT NULL,
//                image BLOB
//            )
//        """.trimIndent()
//
//        val SQL_CREATE_BLACKLIST = """
//            CREATE TABLE IF NOT EXISTS Blacklist (
//                hash TEXT PRIMARY KEY,
//                title TEXT NOT NULL,
//                artist TEXT NOT NULL,
//                album TEXT NOT NULL,
//                albumartist TEXT NOT NULL,
//                song TEXT NOT NULL,
//                image BLOB
//            )
//        """.trimIndent()
//
//        db.execSQL(SQL_CREATE_SONGLIST)
//        db.execSQL(SQL_CREATE_BLACKLIST)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE SongList ADD COLUMN newColumn TEXT DEFAULT 'default_value'")
        }
    }

    internal open fun createTable(db: SQLiteDatabase, table_name: String, columns: Map<String, String>? = null ) {

        val SQL_CREATE_TABLE = if (columns == null) {
            """
            CREATE TABLE IF NOT EXISTS $table_name (
                hash TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                artist TEXT NOT NULL,
                album TEXT NOT NULL,
                albumartist TEXT NOT NULL,
                song TEXT NOT NULL,
                image BLOB
            )
        """.trimIndent()
        } else {
            val columnsDefinition = columns.entries.joinToString(", ") { "${it.key} ${it.value}" }
            "CREATE TABLE IF NOT EXISTS $table_name ($columnsDefinition)"
        }

        try {
            db.execSQL(SQL_CREATE_TABLE)
            Log.d("DatabaseHelper", "Table $table_name created successfully.")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error creating table $table_name: ${e.message}")
        }
    }

    internal open fun ifExist(db: SQLiteDatabase, tableName: String, song: Song): Boolean {
        val cursor = db.rawQuery("SELECT 1 FROM $tableName WHERE hash = ?", arrayOf(song.getHash().toString()))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    internal open fun isBlacklisted(db: SQLiteDatabase, song: Song): Boolean {
        return ifExist(db, "blacklist", song)
    }

    internal open fun blacklist(db: SQLiteDatabase, table_name: String, song: Song) {

        val songCursor = db.rawQuery("SELECT * FROM $table_name WHERE hash = ?", arrayOf(song.getHash().toString()))
        if (songCursor.moveToFirst()) {
            val values = ContentValues()
            for (i in 0 until songCursor.columnCount) {
                val columnName = songCursor.getColumnName(i)
                val columnType = songCursor.getType(i) // Get the type of the current column

                // Check the column type and get the corresponding value
                when (columnType) {
                    Cursor.FIELD_TYPE_STRING -> {
                        if (songCursor.isNull(i)) {
                            values.putNull(columnName)  // Handle null values explicitly
                        } else {
                            values.put(columnName, songCursor.getString(i))
                        }
                    }
                    Cursor.FIELD_TYPE_INTEGER -> {
                        if (songCursor.isNull(i)) {
                            values.putNull(columnName)   // Handle null values explicitly
                        } else {
                            values.put(columnName, songCursor.getInt(i))
                        }
                    }
                    Cursor.FIELD_TYPE_FLOAT -> {
                        if (songCursor.isNull(i)) {
                            values.putNull(columnName)   // Handle null values explicitly
                        } else {
                            values.put(columnName, songCursor.getFloat(i) )
                        }
                    }
                    Cursor.FIELD_TYPE_BLOB -> {
                        if (songCursor.isNull(i)) {
                            values.putNull(columnName)   // Handle null values explicitly for blobs
                        } else {
                            values.put(columnName, songCursor.getBlob(i))
                        }
                    }
                    else -> {
                        // If it's an unsupported type, you can log an error or handle it differently
                        Log.e("Cursor", "Unsupported column type for $columnName")
                    }
                }
            }
            db.insert("blacklist", null, values)
            db.delete(table_name, "hash = ?", arrayOf(song.getHash().toString()))
        }
        songCursor.close()
    }

    internal open fun appendToTable(db: SQLiteDatabase, table_name: String, song: Song) {

        val values = ContentValues().apply {
            put("title", song.getTitle())
            put("artist", song.getArtist())
            put("album", song.getAlbum())
            put("albumArtist", song.getAlbumArtist())
            put("song", song.jsonify())
            put("image", song.getImgSrc())
        }
        db.insert(table_name, null, values)
    }

    internal open fun updateCollection(db: SQLiteDatabase, table_name: String, song: Song) {

        val values = ContentValues().apply {
            put("title", song.getTitle())
            put("artist", song.getArtist())
            put("album", song.getAlbum())
            put("albumArtist", song.getAlbumArtist())
            put("song", song.jsonify())
            put("image", song.getImgSrc())
        }
        db.update(table_name, values, "hash = ?", arrayOf(song.getHash().toString()))

        // Update blacklist if necessary
        if (isBlacklisted(db, song)) {
            db.update("blacklist", values, "hash = ?", arrayOf(song.getHash().toString()))
        }
    }

}

