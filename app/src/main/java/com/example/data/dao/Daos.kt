package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY createdAt ASC")
    fun getTasksForDateFlow(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksDirect(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updated DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesDirect(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_logs")
    fun getAllLogsFlow(): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs")
    suspend fun getAllLogsDirect(): List<PrayerLogEntity>

    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getLogsForDateFlow(date: String): Flow<List<PrayerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PrayerLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<PrayerLogEntity>)

    @Query("DELETE FROM prayer_logs WHERE date = :date AND prayerId = :prayerId")
    suspend fun deleteLog(date: String, prayerId: String)

    @Query("SELECT * FROM prayer_times")
    fun getAllTimesFlow(): Flow<List<PrayerTimeEntity>>

    @Query("SELECT * FROM prayer_times")
    suspend fun getAllTimesDirect(): List<PrayerTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTime(time: PrayerTimeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimes(times: List<PrayerTimeEntity>)

    @Query("DELETE FROM prayer_times WHERE date = :date AND prayerId = :prayerId")
    suspend fun deleteTime(date: String, prayerId: String)
}

@Dao
interface LabelDao {
    @Query("SELECT * FROM app_labels")
    fun getAllLabelsFlow(): Flow<List<AppLabelEntity>>

    @Query("SELECT * FROM app_labels")
    suspend fun getAllLabelsDirect(): List<AppLabelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabel(label: AppLabelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabels(labels: List<AppLabelEntity>)
}
