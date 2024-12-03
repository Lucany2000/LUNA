package com.luna.data

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Accumulators


class BackUpDB {

    private val database: MongoDatabase
    private lateinit var collection: MongoCollection<Document>

    private val client = MongoClients.create("mongodb://localhost:27017")

    init {
        database = client.getDatabase("myDatabase")
    }

    private fun connectToCollection(target: String) {
        if(!database.listCollectionNames().any { it == target }){
            database.createCollection(target)
        }
        collection = database.getCollection(target)
    }

    fun ifExist(song: Song, target: String): Boolean {
        connectToCollection(target)
        val filter = Document("id", song.getId())
        val result = collection.find(filter).firstOrNull()
        return result != null
    }

    fun isBlacklisted(song: Song): Boolean{
        return ifExist(song, "blacklist")
    }

    fun blacklist(song: Song) {
        connectToCollection("SongList")
        val filter = Document("song", song)
        val document = collection.find(filter).firstOrNull()

        if (document != null) {
            collection.deleteOne(document)

            connectToCollection("blacklist")
            collection.insertOne(document)
        }
    }

    fun appendToCollection(song: Song) {
        connectToCollection("SongList")
        val entry = Document("id", song.getId())
            .append("name", song.getName())
            .append("title", song.getTitle())
            .append("artist", song.getArtist())
            .append("artistId", song.getArtistId())
            .append("album", song.getAlbum())
            .append("albumId", song.getAlbumId())
            .append("albumArtist", song.getAlbumArtist())
            .append("track", song.getTrack())
            .append("mime", song.getMime())
            .append("data", song.getData())
            .append("uri", song.getUri())
            .append("image", song.getImgSrc())
            .append("song", song)
        collection.insertOne(entry)
    }

    fun updateCollection(song: Song) {
        connectToCollection("SongList")
        val filter = Document("id", song)

        val update = Document("\$set",
            Document("title", song.getTitle())
            .append("artist", song.getArtist())
            .append("album", song.getAlbum())
            .append("image", song.getImgSrc())
        )

        collection.updateOne(filter, update)

        connectToCollection("blacklist")
        val blacklist = collection.find(filter).firstOrNull()

        if (blacklist != null) {
            collection.updateOne(filter, update)
        }

        /*
        update only if files are from local or external drive

        write once (append only) if files are from apps (soundcloud, spotify, youtube, etc).
        extremely rare exceptions for update

         */

    }

    fun searchAlgo(input: String): MutableMap<String, MutableSet<Map<String, String?>>> {
        connectToCollection("SongList")

        val titleSearch = Filters.regex("title", ".*$input.*", "i")
        val artistSearch = Filters.regex("artist", ".*$input.*", "i")
        val albumSearch = Filters.regex("album", ".*$input.*", "i")

        val combinedFilter = Filters.or(titleSearch, artistSearch, albumSearch)

        val results = collection.find(combinedFilter).toList()

        val combinedMap: MutableMap<String, MutableSet<Map<String, String?>>> = mutableMapOf()

        results.forEach { doc ->
            // Get values from the document
            val title = doc.getString("title")
            val artistId = doc.getString("artistId")
            val artist = doc.getString("artist")
            val albumId = doc.getString("albumId")
            val album = doc.getString("album")
            val song = doc.getString("song")

            // Add to the combined map for title
            combinedMap.computeIfAbsent(title) { mutableSetOf() }.add(mapOf("song" to song))

            // Add to the combined map for artist
            combinedMap.computeIfAbsent(artist) { mutableSetOf() }.add(mapOf("artistId" to artistId, "artist" to artist))

            // Add to the combined map for album
            combinedMap.computeIfAbsent(album) { mutableSetOf() }.add(mapOf("albumId" to albumId, "album" to album, "artistId" to artistId, "artist" to artist))
        }

        return combinedMap
    }

    fun getArtistAlbumSongs(): MutableList<Document> {
        connectToCollection("SongList")

        val pipeline = listOf(
            Aggregates.group( "\$artist",
                Accumulators.addToSet("albums", Document("album", "\$album").append("songs", Document("\$addToSet", "\$song")))
            ),
            Aggregates.project(Document("_id", 0).append("artist", "\$_id").append("albums", "\$albums"))
        ) // Perform the aggregation

        val results = collection.aggregate(pipeline).iterator()
        val aggregatedResults = mutableListOf<Document>()
        results.forEachRemaining { document -> aggregatedResults.add(document) }
        results.close()
        closeClient()

        return aggregatedResults
    }


    fun deleteEntry(entry: Document) {
        collection.deleteOne(entry)
        closeClient()
    }

    fun clearCollection() {
        collection.drop()
        closeClient()
    }

    fun closeClient() {
        client.close()
    }
}

