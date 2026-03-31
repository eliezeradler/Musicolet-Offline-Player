package com.example.offlineplayer.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity

    @Query("SELECT DISTINCT folderPath FROM songs ORDER BY folderPath ASC")
    suspend fun getAllFolders(): List<String>

    @Query("SELECT * FROM songs WHERE folderPath = :folderPath ORDER BY title ASC")
    suspend fun getSongsByFolder(folderPath: String): List<SongEntity>

    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist ASC")
    suspend fun getAllArtists(): List<String>

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long)

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 50")
    suspend fun getMostPlayed(): List<SongEntity>

    @Query("DELETE FROM songs")
    suspend fun deleteAll()
}


