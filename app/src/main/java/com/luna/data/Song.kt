package com.luna.data

import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.json.JSONObject


//TODO: resolve plugin issue by updating Android studio to the latest ver.

//@Serializable
data class Song
    (private val hash: String?,
    private var id: Long,
    private val name: String,
    private var title: String,
    private val artist: String,
    private val artistId: Long,
    private val album: String,
    private val albumId: Long,
    private val albumartist: String,
    private val track: Long,
    private val mime: String,
    private val isDownload: Long,
    private val data: String,
     private val uri: Uri
//     @Serializable(with = UriSerializer::class) private val uri: Uri
            ) {
        fun getId(): Long {
        return id
    }
    fun getName(): String {
        return name
    }
    fun getTitle(): String {
        return title
    }
    fun getArtist(): String {
        return artist
    }
    fun getArtistId(): Long {
        return artistId
    }
    fun getAlbum(): String {
        return album
    }
    fun getAlbumId(): Long {
        return albumId
    }
    fun getAlbumArtist(): String {
        return albumartist
    }
    fun getTrack(): Long {
        return track
    }
    fun getMime(): String {
        return mime
    }
    fun getIsDownload(): Long {
        return isDownload
    }
    fun getData(): String {
        return data
    }
    fun getUri(): Uri {
        return uri
    }

    fun getImgSrc(): String {
        //TODO: Update later
        return "img src"
    }

    fun getHash(): String? {
        return hash
    }


    //TODO: investigate on how ids and titles appear

    fun setId(newId: Long) {
        id = newId
    }

    fun setTitle(name: String) {

    }

    fun jsonify(): String {
        return Json.encodeToString(this) // Convert this object to a JSON string
    }

    fun serialize(): String {
        val jsonObject = JSONObject()
        jsonObject.put("hash", hash)
        jsonObject.put("id", id)
        jsonObject.put("name", name)
        jsonObject.put("title", title)
        jsonObject.put("artist", artist)
        jsonObject.put("artistId", artistId)
        jsonObject.put("album", album)
        jsonObject.put("albumId", albumId)
        jsonObject.put("albumartist", albumartist)
        jsonObject.put("track", track)
        jsonObject.put("mime", mime)
        jsonObject.put("isDownload", isDownload)
        jsonObject.put("data", data)
        jsonObject.put("uri", uri.toString())
        return jsonObject.toString()
    }

    companion object {
        fun deserialize(jsonString: String): Song {
            val jsonObject = JSONObject(jsonString)
            return Song(
                hash = jsonObject.optString("hash"),
                id = jsonObject.getLong("id"),
                name = jsonObject.getString("name"),
                title = jsonObject.getString("title"),
                artist = jsonObject.getString("artist"),
                artistId = jsonObject.getLong("artistId"),
                album = jsonObject.getString("album"),
                albumId = jsonObject.getLong("albumId"),
                albumartist = jsonObject.getString("albumartist"),
                track = jsonObject.getLong("track"),
                mime = jsonObject.getString("mime"),
                isDownload = jsonObject.getLong("isDownload"),
                data = jsonObject.getString("data"),
                uri = Uri.parse(jsonObject.getString("uri"))
            )
        }
    }

}

//object UriSerializer : KSerializer<Uri> {
//
//    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)
//
//    override fun serialize(encoder: Encoder, value: Uri) {
//        // Convert Uri to string for serialization
//        encoder.encodeString(value.toString())
//    }
//
//    override fun deserialize(decoder: Decoder): Uri {
//        // Convert string back to Uri during deserialization
//        return Uri.parse(decoder.decodeString())
//    }
//}


