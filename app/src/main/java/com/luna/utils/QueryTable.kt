package com.luna.utils

import android.content.Context
import com.luna.data.MainDatabase
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.luna.data.Song
import kotlinx.serialization.json.Json


interface QueryTableInterface {
    fun getSongs(db: SQLiteDatabase, artist: String? = null, album: String? = null): List<Song>

    fun getArtists(db: SQLiteDatabase): Set<String>

    fun getAlbums(db: SQLiteDatabase): Set<Pair<String, String>>

    fun searchAlgo(db: SQLiteDatabase, input: String): Map<String, Set<Any>>

}



internal class QueryTable(context: Context): MainDatabase(context), QueryTableInterface {

    override fun getSongs(db: SQLiteDatabase, artist: String?, album: String?): List<Song> {

        val baseQuery = StringBuilder("SELECT song FROM SongList")

        val queryArgs = mutableListOf<String>()

        when {
            artist != null && album == null -> {
                baseQuery.append(" WHERE artist = ?")
                queryArgs.add(artist)
            }
            artist != null && album != null -> {
                baseQuery.append(" WHERE COALESCE(albumartist, artist) = ? AND album = ?")
                queryArgs.add(artist)
                queryArgs.add(album)
            }
        }

        val cursor = db.rawQuery(baseQuery.toString(), queryArgs.toTypedArray())

//        val query = """
//        SELECT *
//        FROM SongList """
//
//        val cursor = db.rawQuery(query, null)

        val results = mutableListOf<Song>()
        while (cursor.moveToNext()) {
            val serializedSong = cursor.getString(cursor.getColumnIndexOrThrow("song"))
            val songObj = Song.deserialize(serializedSong)
//            val songObj = Json.decodeFromString<Song>(serializedSong) // Deserialize back to Song
            results.add(songObj)
        }

        cursor.close()

        return results
    }

    override fun getArtists(db: SQLiteDatabase): Set<String> {
        val query = """
        SELECT artist, albumartist
        FROM SongList """

        val cursor = db.rawQuery(query, null)

        val results = mutableSetOf<String>()

        while (cursor.moveToNext()) {
            val artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"))
            val albumArtist = cursor.getString(cursor.getColumnIndexOrThrow("albumartist"))
            results.add(artist)
            results.add(albumArtist)
        }

        cursor.close()

        return results
    }

    override fun getAlbums(db: SQLiteDatabase): Set<Pair<String, String>> {
        val query = """
        SELECT album, COALESCE(albumartist, artist) as albumartist
        FROM SongList """

        val cursor = db.rawQuery(query, null)

        val results = mutableSetOf<Pair<String, String>>()

        while (cursor.moveToNext()) {
            val album = cursor.getString(cursor.getColumnIndexOrThrow("album"))
            val albumArtist = cursor.getString(cursor.getColumnIndexOrThrow("albumartist"))
            results.add(Pair(album, albumArtist))
        }

        cursor.close()

        return results
    }


    override fun searchAlgo(db: SQLiteDatabase, input: String): Map<String, Set<Any>> {

        val query = """
        SELECT song FROM SongList 
        WHERE title LIKE ? OR COALESCE(albumartist, artist) LIKE ? OR album LIKE ?
    """
        val cursor = db.rawQuery(query, arrayOf("%$input%", "%$input%", "%$input%"))

        val combinedMap = mutableMapOf<String, MutableSet<Any>>()
        while (cursor.moveToNext()) {
            val serializedSong = cursor.getString(cursor.getColumnIndexOrThrow("song"))
            val artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"))
            val album = cursor.getString(cursor.getColumnIndexOrThrow("album"))

            combinedMap.computeIfAbsent("song") { mutableSetOf() }.add(Song.deserialize(serializedSong)) //Json.decodeFromString<Song>(song)
            combinedMap.computeIfAbsent("artist") { mutableSetOf() }.add(artist)
            combinedMap.computeIfAbsent("artist") { mutableSetOf() }.add(album)
        }
        cursor.close()
        return combinedMap
    }
}