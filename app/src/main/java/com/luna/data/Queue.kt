package com.luna.data

import kotlin.collections.Map

// TODO: Might to revist in later issues

object Queue {

    private var defaultQ: Map<Int, Song> = mapOf()

    private var currentQ: MutableMap<Int, Song> = mutableMapOf()

    private var current: Int = 0


    public fun defaultify(songList: List<Song>) {
        defaultQ = songList.mapIndexed { index, value ->
            (index + 1) to value
        }.toMap()
    }

//    public fun setCurrent(audio: Song) {
//
//    }

    public fun getDefault(): Map<Int, Song> {
        return defaultQ
    }

    public fun getCurrentQ(): MutableMap<Int, Song> {
        return currentQ
    }

    public fun getCurrentS(): Song? {
        return currentQ[current]
    }

}
