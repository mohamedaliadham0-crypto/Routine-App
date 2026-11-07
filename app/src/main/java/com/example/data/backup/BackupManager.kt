package com.example.data.backup

import com.example.data.model.*
import com.example.data.repository.StudyRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

object BackupManager {

    suspend fun exportJson(repository: StudyRepository, outputStream: OutputStream) {
        val tasks = repository.getAllTasksDirect()
        val notes = repository.getAllNotesDirect()
        val logs = repository.getAllPrayerLogsDirect()
        val times = repository.getAllPrayerTimesDirect()
        val labels = repository.getAllLabelsDirect()

        val root = JSONObject()

        // Tasks array
        val taskArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("date", t.date)
                put("priority", t.priority)
                put("subject", t.subject)
                put("notes", t.notes)
                put("revision", t.revision)
                put("revised", t.revised)
                put("done", t.done)
            }
            taskArray.put(obj)
        }
        root.put("tasks", taskArray)

        // Notes array
        val noteArray = JSONArray()
        notes.forEach { n ->
            val obj = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("body", n.body)
                put("updated", n.updated)
            }
            noteArray.put(obj)
        }
        root.put("notes", noteArray)

        // PrayerLog map: { "YYYY-MM-DD_prayer": "done" }
        val prayerLogObj = JSONObject()
        logs.forEach { log ->
            prayerLogObj.put("${log.date}_${log.prayerId}", log.status)
        }
        root.put("prayerLog", prayerLogObj)

        // PrayerTimes map: { "YYYY-MM-DD_prayer": "HH:mm" }
        val prayerTimesObj = JSONObject()
        times.forEach { pt ->
            prayerTimesObj.put("${pt.date}_${pt.prayerId}", pt.customTime)
        }
        root.put("prayerTimes", prayerTimesObj)

        // Labels map: { "l1": "TODAY'S PROGRESS", ... }
        val labelsObj = JSONObject()
        labels.forEach { l ->
            labelsObj.put(l.key, l.value)
        }
        root.put("labels", labelsObj)

        outputStream.bufferedWriter().use { it.write(root.toString(2)) }
    }

    suspend fun exportCsv(repository: StudyRepository, outputStream: OutputStream) {
        val tasks = repository.getAllTasksDirect()
        outputStream.bufferedWriter().use { writer ->
            writer.write("Date,Task,Subject,Priority,Done,Revision,Notes\n")
            tasks.forEach { t ->
                val line = listOf(
                    t.date,
                    t.name.replace("\"", "\"\""),
                    t.subject.replace("\"", "\"\""),
                    t.priority,
                    if (t.done) "Yes" else "No",
                    if (t.revision) (if (t.revised) "Done" else "Pending") else "None",
                    t.notes.replace("\"", "\"\"")
                ).joinToString(",") { "\"$it\"" }
                writer.write(line + "\n")
            }
        }
    }

    suspend fun importJson(repository: StudyRepository, inputStream: InputStream): Int {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonString)

        if (!root.has("tasks")) {
            throw IllegalArgumentException("Invalid backup: missing 'tasks' key")
        }

        var importedTasksCount = 0
        val taskEntities = mutableListOf<TaskEntity>()
        val taskArray = root.optJSONArray("tasks")
        if (taskArray != null) {
            for (i in 0 until taskArray.length()) {
                val obj = taskArray.getJSONObject(i)
                val task = TaskEntity(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    name = obj.optString("name", "Untitled"),
                    date = obj.optString("date", ""),
                    priority = obj.optString("priority", "Normal"),
                    subject = obj.optString("subject", ""),
                    notes = obj.optString("notes", ""),
                    revision = obj.optBoolean("revision", false),
                    revised = obj.optBoolean("revised", false),
                    done = obj.optBoolean("done", false)
                )
                if (task.date.isNotEmpty()) {
                    taskEntities.add(task)
                }
            }
            if (taskEntities.isNotEmpty()) {
                repository.insertTasks(taskEntities)
                importedTasksCount = taskEntities.size
            }
        }

        // Import notes
        val noteArray = root.optJSONArray("notes")
        if (noteArray != null) {
            val noteEntities = mutableListOf<NoteEntity>()
            for (i in 0 until noteArray.length()) {
                val obj = noteArray.getJSONObject(i)
                val note = NoteEntity(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    title = obj.optString("title", "Untitled Note"),
                    body = obj.optString("body", ""),
                    updated = obj.optLong("updated", System.currentTimeMillis())
                )
                noteEntities.add(note)
            }
            if (noteEntities.isNotEmpty()) {
                repository.insertNotes(noteEntities)
            }
        }

        // Import prayer logs
        val prayerLogObj = root.optJSONObject("prayerLog")
        if (prayerLogObj != null) {
            val logEntities = mutableListOf<PrayerLogEntity>()
            val keys = prayerLogObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val status = prayerLogObj.optString(key, "")
                val split = key.split("_")
                if (split.size == 2 && status.isNotEmpty()) {
                    logEntities.add(PrayerLogEntity(date = split[0], prayerId = split[1], status = status))
                }
            }
            if (logEntities.isNotEmpty()) {
                repository.insertPrayerLogs(logEntities)
            }
        }

        // Import prayer times
        val prayerTimesObj = root.optJSONObject("prayerTimes")
        if (prayerTimesObj != null) {
            val timeEntities = mutableListOf<PrayerTimeEntity>()
            val keys = prayerTimesObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val time = prayerTimesObj.optString(key, "")
                val split = key.split("_")
                if (split.size == 2 && time.isNotEmpty()) {
                    timeEntities.add(PrayerTimeEntity(date = split[0], prayerId = split[1], customTime = time))
                }
            }
            if (timeEntities.isNotEmpty()) {
                repository.insertPrayerTimes(timeEntities)
            }
        }

        // Import labels
        val labelsObj = root.optJSONObject("labels")
        if (labelsObj != null) {
            val labelEntities = mutableListOf<AppLabelEntity>()
            val keys = labelsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = labelsObj.optString(key, "")
                if (value.isNotEmpty()) {
                    labelEntities.add(AppLabelEntity(key = key, value = value))
                }
            }
            if (labelEntities.isNotEmpty()) {
                repository.insertLabels(labelEntities)
            }
        }

        return importedTasksCount
    }
}
