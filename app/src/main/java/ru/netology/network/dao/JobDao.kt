package ru.netology.network.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.network.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity WHERE userId =:userId ORDER BY start DESC")
    fun getJobsByIdUser(userId: Long): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity ORDER BY start DESC")
    fun getJobs(): Flow<List<JobEntity>>

    @Query("SELECT COUNT(*) == 0 FROM JobEntity WHERE userId =:userId")
    suspend fun isEmpty(userId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}