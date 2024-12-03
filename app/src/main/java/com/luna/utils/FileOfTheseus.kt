package com.luna.utils

import android.database.sqlite.SQLiteDatabase
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

import android.util.Log

object FileOfTheseus {

        // Function to extract raw audio and hash it
    fun extractRawAudioAndHash(filePath: String, callback: (String?) -> Unit) {
        val command = "-i $filePath -vn -f s16le -acodec pcm_s16le -"

        val outputStream = ByteArrayOutputStream()

        FFmpegKit.executeAsync(command) { session: FFmpegSession ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                // Get the raw audio data as byte array

                val digest = MessageDigest.getInstance("SHA-256")

                val rawAudioBytes = outputStream.toByteArray()

                val hash = digest.digest(rawAudioBytes)

                callback(hash.toString(Charsets.ISO_8859_1))

                Log.d("FFmpeg", "Hash of $filePath: ${hash.joinToString("") { "%02x".format(it) }}")
            } else {
                Log.e("FFmpeg", "Error during extraction: ${session.returnCode}")

                callback(null)
            }
        }

    //            return hashBytes(outputStream.toByteArray())
    }

    fun sqltest(db: SQLiteDatabase, filePath: String) {
        extractRawAudioAndHash(filePath) { hash ->
            val cursor = db.rawQuery(
                "SELECT 1 FROM AudioFiles WHERE hash = ?",
                arrayOf(hash)
            )

            if (!cursor.moveToFirst()) {
                // Process the result
            }
            cursor.close()

        }
    }

//    extractRawAudioAndHash(filePath) { hash ->
//        if (hash != null) {
//            // Process the hash (e.g., print it or compare it with another file)
//            println("Hash: ${hash.joinToString("") { "%02x".format(it) }}")
//        } else {
//            println("Error processing the file.")
//        }
//    }


    // Function to compare two audio files by comparing hash bytes
//        private fun compareAudioFiles(file1Path: String, file2Path: String): Boolean {
//            val file1Hash = extractRawAudioAndHash(file1Path)
//            val file2Hash = extractRawAudioAndHash(file2Path)
//
//            // Compare the hash bytes directly
//
//            return file1Hash contentEquals file2Hash
//        }
}

//compareAudioFiles(file1Path, file2Path)