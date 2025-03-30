package com.luna.utils

import android.util.Log
import android.widget.LinearLayout
import android.widget.ScrollView
import com.luna.data.Song

object BackEnd {

    //Sorting Algo.

    //TODO: Test whether using the new sort, despite added overhead, is better than its single type counterparts

//    fun sortSongs(audioFiles: List<Song>): List<Song> {
//        val customComparator = Comparator<Song> { audio1, audio2 ->
//            val title1 = audio1.getTitle() ?: ""
//            val title2 = audio2.getTitle() ?: ""
//
//            // Ignore case and handle 'A' and 'The' cases
//            val title1WithoutPrefix = removePrefix(title1)
//            val title2WithoutPrefix = removePrefix(title2)
//
//            // Compare the titles without 'A' or 'The'
//            title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)
//        }
//
//        val sortedAudioFiles = audioFiles.sortedWith(customComparator)
//        return sortedAudioFiles
//    }

    //    fun generalSort(list: List<String>): List<String> {
//        val customComparator = Comparator<Song> { item1, item2 ->
//            val title1 = item1 ?: ""
//            val title2 = item2 ?: ""
//
//            // Ignore case and handle 'A' and 'The' cases
//            val title1WithoutPrefix = removePrefix(title1)
//            val title2WithoutPrefix = removePrefix(title2)
//
//            // Compare the titles without 'A' or 'The'
//            title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)
//        }
//
//        val sortedAudioFiles = list.sortedWith(customComparator)
//        return sortedAudioFiles
//    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> sort(list: List<T>): List<T> {

        return when (T::class) {
            Song::class -> {
                val sortedSongs = (list as List<Song>).sortedWith { item1, item2 ->
                    val title1 = item1.getTitle() ?: ""
                    val title2 = item2.getTitle() ?: ""

                    // Remove common prefixes like "A" and "The" before sorting
                    val title1WithoutPrefix = removePrefix(title1)
                    val title2WithoutPrefix = removePrefix(title2)

                    title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)
                }
                sortedSongs as List<T>  // Safe cast
            }

            String::class -> {
                val sortedSongs = (list as List<String>).sortedWith { item1, item2 ->
                    val title1 = item1 ?: ""
                    val title2 = item2 ?: ""

                    // Remove common prefixes like "A" and "The" before sorting
                    val title1WithoutPrefix = removePrefix(title1)
                    val title2WithoutPrefix = removePrefix(title2)

                    title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)
                }
                sortedSongs as List<T>  // Safe cast
            }

            Pair::class -> {
                val sortedSongs = (list as List<Pair<String, String>>).sortedWith { item1, item2 ->
                    val title1 = item1.first ?: ""
                    val title2 = item2.first ?: ""

                    // Remove common prefixes like "A" and "The" before sorting
                    val title1WithoutPrefix = removePrefix(title1)
                    val title2WithoutPrefix = removePrefix(title2)

                    val titleComparison = title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)

                    if (titleComparison == 0) {

                        val title1WithoutPrefix = removePrefix(item1.second)
                        val title2WithoutPrefix = removePrefix(item2.second)

                        title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)

                    } else {
                        titleComparison
                    }

                }
                sortedSongs as List<T>  // Safe cast
            }

            else -> list
        }
    }

    fun removePrefix(title: String): String {
        val lowerCaseTitle = title.lowercase()
        return when {
            lowerCaseTitle.startsWith("the ") -> title.substring(4)
            lowerCaseTitle.startsWith("a ") -> title.substring(2)
            else -> title
        }
    }

    //TODO: Maybe
//    fun removeTrailingDot(title: String): String {
//        val lowerCaseTitle = title.lowercase()
//        return when {
//            lowerCaseTitle.startsWith("the ") -> title.substring(4)
//            lowerCaseTitle.startsWith("a ") -> title.substring(2)
//            else -> title
//        }
//    }


    //Character Line
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> createKnownAlphabet(list: List<T>): LinkedHashSet<Char> {
        val uniqueChars = LinkedHashSet<Char>()
        // Iterate through songs to get unique first characters
        return when (T::class) {
            Song::class -> {
                for (item in (list as List<Song>)) {
                    val sanitizedSong = removePrefix(item.getTitle())
                    val firstChar = sanitizedSong.trimStart().uppercase()[0]
                    uniqueChars.add(firstChar)
                    Log.d("UniqueChars", "$uniqueChars")
                }
                uniqueChars
            }

            String::class -> {
                for (item in (list as List<String>)) {
                    val sanitizedSong = removePrefix(item)
                    val firstChar = sanitizedSong.trimStart().uppercase()[0]
                    uniqueChars.add(firstChar)
                    Log.d("UniqueChars", "$uniqueChars")
                }
                uniqueChars
            }

            Pair::class -> {
                for (item in (list as List<Pair<String, String>>)) {
                    val sanitizedSong = removePrefix(item.first)
                    val firstChar = sanitizedSong.trimStart().uppercase()[0]
                    uniqueChars.add(firstChar)
                    Log.d("UniqueChars", "$uniqueChars")
                }
                uniqueChars
            }

            else -> uniqueChars
        }
    }

    fun scrollToWordStartingWith(letter: String, letterToFirstInstance: MutableMap<String, LinearLayout> ,scrollView: ScrollView) {
        val textView: LinearLayout? = letterToFirstInstance[letter]
        textView?.let {
            val scrollToY = it.top
            scrollView.post {
                scrollView.smoothScrollTo(0, scrollToY)
            }
        }
    }
}