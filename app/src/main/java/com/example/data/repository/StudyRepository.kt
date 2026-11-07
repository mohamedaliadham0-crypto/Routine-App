package com.example.data.repository

import com.example.data.dao.*
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val database: AppDatabase) {
    private val taskDao = database.taskDao()
    private val noteDao = database.noteDao()
    private val prayerDao = database.prayerDao()
    private val labelDao = database.labelDao()

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasksFlow()
    fun getTasksForDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksForDateFlow(date)
    suspend fun getAllTasksDirect(): List<TaskEntity> = taskDao.getAllTasksDirect()
    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun insertTasks(tasks: List<TaskEntity>) = taskDao.insertTasks(tasks)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: String) = taskDao.deleteTaskById(id)
    suspend fun clearAllTasks() = taskDao.clearAllTasks()

    // Notes
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotesFlow()
    suspend fun getAllNotesDirect(): List<NoteEntity> = noteDao.getAllNotesDirect()
    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)
    suspend fun insertNotes(notes: List<NoteEntity>) = noteDao.insertNotes(notes)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: String) = noteDao.deleteNoteById(id)

    // Prayers
    fun getAllPrayerLogs(): Flow<List<PrayerLogEntity>> = prayerDao.getAllLogsFlow()
    fun getAllPrayerTimes(): Flow<List<PrayerTimeEntity>> = prayerDao.getAllTimesFlow()
    suspend fun getAllPrayerLogsDirect(): List<PrayerLogEntity> = prayerDao.getAllLogsDirect()
    suspend fun getAllPrayerTimesDirect(): List<PrayerTimeEntity> = prayerDao.getAllTimesDirect()
    
    suspend fun setPrayerStatus(date: String, prayerId: String, status: String) {
        if (status.isEmpty()) {
            prayerDao.deleteLog(date, prayerId)
        } else {
            prayerDao.insertLog(PrayerLogEntity(date, prayerId, status))
        }
    }

    suspend fun setCustomPrayerTime(date: String, prayerId: String, time: String) {
        if (time.isEmpty()) {
            prayerDao.deleteTime(date, prayerId)
        } else {
            prayerDao.insertTime(PrayerTimeEntity(date, prayerId, time))
        }
    }

    suspend fun insertPrayerLogs(logs: List<PrayerLogEntity>) = prayerDao.insertLogs(logs)
    suspend fun insertPrayerTimes(times: List<PrayerTimeEntity>) = prayerDao.insertTimes(times)

    // Labels
    fun getAllLabels(): Flow<List<AppLabelEntity>> = labelDao.getAllLabelsFlow()
    suspend fun getAllLabelsDirect(): List<AppLabelEntity> = labelDao.getAllLabelsDirect()
    suspend fun updateLabel(key: String, value: String) = labelDao.insertLabel(AppLabelEntity(key, value))
    suspend fun insertLabels(labels: List<AppLabelEntity>) = labelDao.insertLabels(labels)
}
