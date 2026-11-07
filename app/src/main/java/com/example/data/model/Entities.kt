package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: String, // ISO date string: YYYY-MM-DD
    val priority: String = "Normal", // "High", "Normal", "Low"
    val subject: String = "",
    val notes: String = "",
    val revision: Boolean = false,
    val revised: Boolean = false,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val updated: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_logs", primaryKeys = ["date", "prayerId"])
data class PrayerLogEntity(
    val date: String, // YYYY-MM-DD
    val prayerId: String, // "fajr", "dhuhr", "asr", "maghrib", "isha"
    val status: String // "done", "missed", or "" (reset)
)

@Entity(tableName = "prayer_times", primaryKeys = ["date", "prayerId"])
data class PrayerTimeEntity(
    val date: String, // YYYY-MM-DD
    val prayerId: String, // "fajr", etc.
    val customTime: String // HH:mm
)

@Entity(tableName = "app_labels")
data class AppLabelEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
