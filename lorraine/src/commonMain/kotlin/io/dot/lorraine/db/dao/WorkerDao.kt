package io.dot.lorraine.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import io.dot.lorraine.db.entity.WorkerEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@Dao
@OptIn(ExperimentalUuidApi::class)
internal interface WorkerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(worker: WorkerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workers: List<WorkerEntity>)

    @Upsert
    suspend fun upsert(worker: WorkerEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(worker: WorkerEntity)

    @Delete
    suspend fun delete(worker: WorkerEntity)

    @Delete
    suspend fun delete(workers: List<WorkerEntity>)

    @Query("SELECT * FROM worker WHERE uuid = (:uuidString)")
    suspend fun getWorker(uuidString: String): WorkerEntity?

    @Query("SELECT * FROM worker")
    suspend fun getWorkers(): List<WorkerEntity>

    @Query("SELECT * FROM worker WHERE queue_id = (:queueId)")
    suspend fun getWorkersForQueueId(queueId: String): List<WorkerEntity>

    @Query("SELECT * FROM worker WHERE uuid = (:uuidString)")
    fun getWorkerAsFlow(uuidString: String): Flow<WorkerEntity?>

    @Query("SELECT * FROM worker")
    fun getWorkersAsFlow(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM worker WHERE queue_id = (:queueId)")
    fun getWorkersForQueueIdAsFlow(queueId: String): Flow<List<WorkerEntity>>

}