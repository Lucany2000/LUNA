package com.luna.data

import android.net.Uri

class Song
    (private val id: Long,
    private val name: String,
    private val title: String,
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

    public fun getId(): Long {
        return id
    }

    public fun getName(): String {
        return name
    }

    public fun getTitle(): String {
        return title
    }

    public fun getArtist(): String {
        return artist
    }

    public fun getArtistId(): Long {
        return artistId
    }

    public fun getAlbum(): String {
        return album
    }

    public fun getAlbumId(): Long {
        return albumId
    }

    public fun getAlbumArtist(): String {
        return albumartist
    }

    public fun getTrack(): Long {
        return track
    }

    public fun getMime(): String {
        return mime
    }

    public fun getIsDownload(): Long {
        return isDownload
    }

    public fun getData(): String {
        return data
    }

    public fun getUri(): Uri {
        return uri
    }
}


