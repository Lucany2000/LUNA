package com.luna.utils

import com.luna.data.Song

object BackEnd {
    fun sort(audioFiles: List<Song>): List<Song> {
        val customComparator = Comparator<Song> { audio1, audio2 ->
            val title1 = audio1.getTitle() ?: ""
            val title2 = audio2.getTitle() ?: ""

            // Ignore case and handle 'A' and 'The' cases
            val title1WithoutPrefix = removePrefix(title1)
            val title2WithoutPrefix = removePrefix(title2)

            // Compare the titles without 'A' or 'The'
            title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)
        }

        val sortedAudioFiles = audioFiles.sortedWith(customComparator)
        return sortedAudioFiles
    }

    fun removePrefix(title: String): String {
        val lowerCaseTitle = title.lowercase()
        return when {
            lowerCaseTitle.startsWith("the ") -> title.substring(4)
            lowerCaseTitle.startsWith("a ") -> title.substring(2)
            else -> title
        }
    }

    fun createKnownAlphabet(songList: List<Song>): HashSet<Char> {
        val uniqueChars = HashSet<Char>()

        // Iterate through songs to get unique first characters
        for (song in songList) {
            val sanitizedSong = removePrefix(song.getTitle())
            val firstChar = sanitizedSong.trimStart().uppercase()[0]
            uniqueChars.add(firstChar)
        }
        return uniqueChars

    }
}