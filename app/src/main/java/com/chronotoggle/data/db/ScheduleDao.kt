package com.chronotoggle.data.db

import androidx.room.*
import com.chronotoggle.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules ORDER BY hour ASC, minute ASC")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?

    @Query("SELECT * FROM schedules WHERE isEnabled = 1")
    suspend fun getEnabledSchedules(): List<Schedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Long)
}
