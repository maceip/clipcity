package com.example.data

import kotlinx.coroutines.flow.Flow

class VideoJobRepository(private val videoJobDao: VideoJobDao) {
    val allJobs: Flow<List<VideoJob>> = videoJobDao.getAllJobsFlow()

    suspend fun getJobById(id: Int): VideoJob? = videoJobDao.getJobById(id)

    suspend fun insertJob(job: VideoJob): Long = videoJobDao.insertJob(job)

    suspend fun updateJob(job: VideoJob) = videoJobDao.updateJob(job)

    suspend fun deleteJobById(id: Int) = videoJobDao.deleteJobById(id)

    suspend fun deleteAllJobs() = videoJobDao.deleteAllJobs()
}
