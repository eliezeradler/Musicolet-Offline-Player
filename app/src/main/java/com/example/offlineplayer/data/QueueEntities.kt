package com.example.offlineplayer.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "queues")
data class QueueEntity(
    @PrimaryKey val queueId: Int,
    val name: String,
    val currentSongIndex: Int = 0,
    val currentPlaybackPosition: Long = 0L
)

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val queueId: Int,
    val songId: Long,
    val positionIndex: Int
)

@Dao
interface QueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(queue: QueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("SELECT * FROM queues WHERE queueId = :queueId")
    suspend fun getQueue(queueId: Int): QueueEntity?

    @Query("SELECT * FROM queues ORDER BY queueId ASC")
    suspend fun getAllQueues(): List<QueueEntity>

    @Query("""
        SELECT s.* FROM songs s 
        INNER JOIN queue_items q ON s.id = q.songId 
        WHERE q.queueId = :queueId 
        ORDER BY q.positionIndex ASC
    """)
    suspend fun getSongsForQueue(queueId: Int): List<SongEntity>

    @Query("DELETE FROM queue_items WHERE queueId = :queueId")
    suspend fun clearQueueItems(queueId: Int)

    @Query("UPDATE queues SET currentSongIndex = :index, currentPlaybackPosition = :position WHERE queueId = :queueId")
    suspend fun saveQueueState(queueId: Int, index: Int, position: Long)
}


