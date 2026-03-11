package com.chronotoggle.data.repository

import com.chronotoggle.data.db.ScheduleDao
import com.chronotoggle.data.model.Schedule
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {

    val allSchedules: Flow<List<Schedule>> = dao.getAllSchedules()

    suspend fun getById(id: Long): Schedule? = dao.getScheduleById(id)

    suspend fun getEnabled(): List<Schedule> = dao.getEnabledSchedules()

    suspend fun insert(schedule: Schedule): Long = dao.insertSchedule(schedule)

    suspend fun update(schedule: Schedule) = dao.updateSchedule(schedule)

    suspend fun delete(schedule: Schedule) = dao.deleteSchedule(schedule)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
