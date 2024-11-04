package com.luna.global

import com.luna.data.Song
import java.util.TreeMap
import kotlin.collections.Map

// TODO: Might to revist in later issues

object SongOrder {

    private var defaultOrder: Map<Int, Song> = mapOf()

    private var currentOrder: TreeMap<Int, Song> = TreeMap()

    private var current: MutableList<Any> = mutableListOf()


    fun createDefaultOrder(songList: List<Song>) {
        defaultOrder = songList.mapIndexed { index, value ->
            (index + 1) to value
        }.toMap()
    }

    fun setCurrentOrder(songList: MutableMap<Int, Song>) {
        currentOrder = TreeMap(songList)
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

    fun getCurrentOrder(): TreeMap<Int, Song> {
        return currentOrder
    }

    fun getCurrentSong(): List<Any> {
        return current
    }

}
