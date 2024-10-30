package com.luna.utils

import android.media.MediaPlayer
import android.content.Context
import android.net.Uri

class MusicPlayer(context: Context, uri: Uri) {

    private var player: MediaPlayer

    init {
        player = MediaPlayer.create(context, uri)
    }

    fun play(){
        if (!isPlaying()) {
            player.start()
        }
    }
    fun pause(){
        if (!isPlaying()) {
            player.pause()
        }
    }

    fun resume(){
        play()
    }

    fun stop(){
        player.stop()
    }

    fun release(){
        if (getCurrentPosition() >= getDuration()){
            player.release()
        }
    }

    fun getCurrentPosition(): Int {
        return player.currentPosition
    }

    fun getDuration(): Int{
       return player.duration
    }

    fun seekTo(position: Int) {
        if (position in 0..player.duration) {
            player.seekTo(position)
        } else {
            println("Position out of bounds")
        }
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun isLooping(): Boolean {
        return player.isLooping
    }

    // TODO: private fun milliToTime()


}
