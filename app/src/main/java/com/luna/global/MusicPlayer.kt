package com.luna.global

import android.media.MediaPlayer
import android.content.Context
import android.net.Uri

object MusicPlayer {

    private lateinit var player: MediaPlayer

    fun createPlayer (context: Context, uri: Uri) {
        player = MediaPlayer.create(context, uri)
    }

    fun checkIfPlayerEmpty(): MediaPlayer? {
        return if (MusicPlayer::player.isInitialized) player else null
    }

    fun play(){
        if (!isPlaying()) {
            player.start()
        }
    }
    fun pause(){
        if (isPlaying()) {
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
