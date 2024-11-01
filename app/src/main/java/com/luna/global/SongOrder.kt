package com.luna.global

import com.luna.data.Song
import kotlin.collections.Map

// TODO: Might to revist in later issues

object SongOrder {

    private var defaultOrder: Map<Int, Song> = mapOf()

    private var currentOrder: MutableMap<Int, Song> = mutableMapOf()

    private var current: MutableList<Any> = mutableListOf()


    fun createDefaultOrder(songList: List<Song>) {
        defaultOrder = songList.mapIndexed { index, value ->
            (index + 1) to value
        }.toMap()
    }

    fun setCurrentOrder(songList: MutableMap<Int, Song>) {
        currentOrder = songList
    }

    fun setCurrentSong(song: Song) {
        defaultOrder.entries.find { it.value.getTitle() == song.getTitle() }?.key?.let {
            current.add(
                it
            )
        }
        song.getTitle().let { current.add(it) }
    }

    fun getDefault(): Map<Int, Song> {
        return defaultOrder
    }

    fun getCurrent(): MutableMap<Int, Song> {
        return currentOrder
    }

    fun getCurrentSong(): List<Any> {
        return current
    }

}
