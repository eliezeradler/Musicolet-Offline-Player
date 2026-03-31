package com.example.offlineplayer.scanner
import android.content.Context
import android.provider.MediaStore
import com.example.offlineplayer.data.SongEntity
import java.io.File

class MediaScanner(private val context: Context) {
    fun scanLocalMedia(): List<SongEntity> {
        val songsList = mutableListOf<SongEntity>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DATE_ADDED)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        context.contentResolver.query(uri, projection, selection, null, "${MediaStore.Audio.Media.TITLE} ASC")?.use { cursor ->
            while (cursor.moveToNext()) {
                val dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) ?: continue
                songsList.add(SongEntity(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown",
                    artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown",
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown",
                    albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)),
                    duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                    dataPath = dataPath,
                    folderPath = File(dataPath).parent ?: "Unknown",
                    dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))
                ))
            }
        }
        return songsList
    }
}


