package com.luna.utils

import android.content.Context
import com.luna.data.MainDatabase
import android.database.sqlite.SQLiteDatabase
import com.luna.data.Song
import kotlinx.serialization.json.Json

open class QueryTable(context: Context): MainDatabase(context) {

    open fun getSongs(db: SQLiteDatabase, table_name: String): List<Song> {

        val query = """
        SELECT song
        FROM $table_name """


        val cursor = db.rawQuery(query, null)

        val results = mutableListOf<Song>()
        if (cursor.moveToFirst()) {
            val serializedSong = cursor.getString(cursor.getColumnIndexOrThrow("songObj"))
            val songObj = Json.decodeFromString<Song>(serializedSong) // Deserialize back to Song
            results.add(songObj)
        }
        cursor.close()

        return results
    }


    open fun searchAlgo(db: SQLiteDatabase, input: String): MutableMap<String, MutableSet<Map<String, String?>>> {

        val query = """
        SELECT song FROM SongList 
        WHERE title LIKE ? OR artist LIKE ? OR album LIKE ?
    """
        val cursor = db.rawQuery(query, arrayOf("%$input%", "%$input%", "%$input%"))

        val combinedMap = mutableMapOf<String, MutableSet<Map<String, String?>>>()
        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"))
            val album = cursor.getString(cursor.getColumnIndexOrThrow("album"))
            val song = cursor.getString(cursor.getColumnIndexOrThrow("song"))

            combinedMap.computeIfAbsent(title) { mutableSetOf() }.add(mapOf("song" to song))
            combinedMap.computeIfAbsent(artist) { mutableSetOf() }.add(mapOf("artist" to artist))
            combinedMap.computeIfAbsent(album) { mutableSetOf() }.add(mapOf("album" to album))
        }
        cursor.close()
        return combinedMap
    }

//    fun getArtist(db: SQLiteDatabase, table_name: String, view: View) {}


    open fun getArtistAlbumSongs(db: SQLiteDatabase, table_name: String): MutableList<Map<String, Any>> {

        val query = """
        SELECT artist, album, GROUP_CONCAT(title) AS songs
        FROM $table_name
        GROUP BY artist, album
    """
        val cursor = db.rawQuery(query, null)

        val aggregatedResults = mutableListOf<Map<String, Any>>()
        while (cursor.moveToNext()) {
            val artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"))
            val album = cursor.getString(cursor.getColumnIndexOrThrow("album"))
            val songs = cursor.getString(cursor.getColumnIndexOrThrow("songs")).split(",")

            aggregatedResults.add(mapOf("artist" to artist, "album" to album, "songs" to songs))
        }
        cursor.close()
        return aggregatedResults
    }
}