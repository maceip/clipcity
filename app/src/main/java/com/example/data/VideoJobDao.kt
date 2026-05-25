package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoJobDao {
    @Query("SELECT * FROM video_jobs ORDER BY timestamp DESC")
    fun getAllJobsFlow(): Flow<List<VideoJob>>

    @Query("SELECT * FROM video_jobs WHERE id = :id")
    suspend fun getJobById(id: Int): VideoJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: VideoJob): Long

    @Update
    suspend fun updateJob(job: VideoJob)

    @Query("DELETE FROM video_jobs WHERE id = :id")
    suspend fun deleteJobById(id: Int)

    @Query("DELETE FROM video_jobs")
    suspend fun deleteAllJobs()
}
