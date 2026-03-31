package com.example.offlineplayer.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val dataPath: String,
    val folderPath: String,
    val dateAdded: Long,
    val playCount: Int = 0,
    val lastPlayed: Long = 0
)


