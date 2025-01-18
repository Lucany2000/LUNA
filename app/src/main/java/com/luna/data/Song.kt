package com.luna.data

import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class Song
    (private var id: Long,
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
     private val uri: Uri) {
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

    //TODO: investigate on how ids and titles appear

    fun setId(newId: Long) {
        id = newId
    }

    fun setTitle(name: String) {

    }

    fun jsonify(): String {
        return Json.encodeToString(this) // Convert this object to a JSON string
    }

}


