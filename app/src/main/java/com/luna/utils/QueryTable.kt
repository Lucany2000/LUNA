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

    fun searchAlgo(db: SQLiteDatabase, input: String): Map<String, Set<*>>

}



internal class QueryTable(context: Context): MainDatabase(context), QueryTableInterface {
    /**
     *
     * @param artist An optional parameter that will search for songs that are from this artist
     *
     * @param album An optional parameter that will search for songs that are a part this album
     *
     */
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


    override fun searchAlgo(db: SQLiteDatabase, input: String): Map<String, Set<*>> {

        val query = """
            SELECT 
                CASE
                    WHEN title LIKE :search THEN song
                END AS song,
                CASE
                    WHEN artist LIKE :search THEN artist
                    WHEN albumartist LIKE :search THEN albumartist
                END AS artist,
                CASE
                    WHEN album LIKE :search THEN 
                        CASE
                            WHEN albumartist IS NOT NULL AND albumartist != '' THEN album || ' - ' || albumartist
                            ELSE album || ' - ' || artist
                        END
                END AS album
            FROM SongList
            WHERE title LIKE :search OR artist LIKE :search OR albumartist LIKE :search OR album LIKE :search
        """

        val cursor = db.rawQuery(query, arrayOf("%$input%"))

        val combinedMap = mutableMapOf<String, MutableSet<*>>()

        val songSet = mutableSetOf<Song>()
        val artistSet = mutableSetOf<String>()
        val albumSet = mutableSetOf<Pair<String?, String?>>()

        cursor.use { c ->
            while (c.moveToNext()) {
                val serializedSong = cursor.getString(cursor.getColumnIndexOrThrow("song"))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"))
//            val albumartist = cursor.getString(cursor.getColumnIndexOrThrow("albumartist"))
                val album = cursor.getString(cursor.getColumnIndexOrThrow("album"))

                serializedSong?.takeIf { it.isNotBlank() }?.let {s -> songSet.add(Song.deserialize(s))}

                artist?.takeIf { it.isNotBlank() }?.let {a -> artistSet.add(a)}

                album?.takeIf { it.isNotBlank() }?.let {a ->
                    val albumPair = a.split(" - ", limit = 2).let { it.firstOrNull() to it.getOrNull(1) }
                    albumSet.add(albumPair)
                }
            }
        }

        combinedMap["song"] = songSet
        combinedMap["artist"] = artistSet
        combinedMap["album"] = albumSet


        return combinedMap
    }
}