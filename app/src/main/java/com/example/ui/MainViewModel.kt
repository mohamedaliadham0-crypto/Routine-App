package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.prayer.EgyptianPrayerCalculator
import com.example.data.prayer.PrayerInfo
import com.example.data.preferences.PreferencesRepository
import com.example.data.repository.StudyRepository
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Calendar : Screen("calendar", "Calendar")
    object Notes : Screen("notes", "Notes")
    object Analytics : Screen("analytics", "Analytics")
    object Settings : Screen("settings", "Settings & Backup")
    object UserManual : Screen("user_manual", "User Manual")
}

data class DayHeatmapItem(
    val date: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val level: Int // 0..4
)

data class SubjectStats(
    val subject: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val progressPct: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository: StudyRepository
    val prefsRepository: PreferencesRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudyRepository(db)
        prefsRepository = PreferencesRepository(application)
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentMonthCursor = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val currentMonthCursor: StateFlow<LocalDate> = _currentMonthCursor.asStateFlow()

    // Preferences
    val themeMode: StateFlow<String> = prefsRepository.themeModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "system"
    )
    val notificationsEnabled: StateFlow<Boolean> = prefsRepository.notificationsEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    // Data flows
    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allNotes: StateFlow<List<NoteEntity>> = repository.getAllNotes().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allPrayerLogs: StateFlow<List<PrayerLogEntity>> = repository.getAllPrayerLogs().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allPrayerTimes: StateFlow<List<PrayerTimeEntity>> = repository.getAllPrayerTimes().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allLabels: StateFlow<List<AppLabelEntity>> = repository.getAllLabels().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Derived states
    val todayDateString: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val todayTasks: StateFlow<List<TaskEntity>> = combine(allTasks, _selectedDate) { tasks, date ->
        tasks.filter { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStreak: StateFlow<Int> = allTasks.map { tasks ->
        calculateStreak(tasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bestStreak: StateFlow<Int> = allTasks.map { tasks ->
        calculateBestStreak(tasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingReviewsCount: StateFlow<Int> = allTasks.map { tasks ->
        tasks.count { it.revision && !it.revised && it.done }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val heatmap14Days: StateFlow<List<DayHeatmapItem>> = allTasks.map { tasks ->
        val list = mutableListOf<DayHeatmapItem>()
        val today = LocalDate.now()
        for (i in 13 downTo 0) {
            val d = today.minusDays(i.toLong())
            val ds = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayTasks = tasks.filter { it.date == ds }
            val done = dayTasks.count { it.done }
            val total = dayTasks.size
            val ratio = if (total > 0) done.toFloat() / total else 0f
            val level = when {
                ratio >= 0.9f && done > 0 -> 4
                ratio >= 0.6f -> 3
                ratio >= 0.3f -> 2
                done > 0 -> 1
                else -> 0
            }
            list.add(DayHeatmapItem(ds, total, done, level))
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectStatsList: StateFlow<List<SubjectStats>> = allTasks.map { tasks ->
        val map = mutableMapOf<String, Pair<Int, Int>>() // subject -> (total, done)
        tasks.forEach { t ->
            val subj = if (t.subject.isBlank()) "No subject" else t.subject
            val current = map.getOrDefault(subj, Pair(0, 0))
            map[subj] = Pair(current.first + 1, current.second + if (t.done) 1 else 0)
        }
        map.map { (subj, pair) ->
            val pct = if (pair.first > 0) (pair.second * 100 / pair.first) else 0
            SubjectStats(subj, pair.first, pair.second, pct)
        }.sortedByDescending { it.totalTasks }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prayerListForSelectedDate: StateFlow<List<PrayerInfo>> = combine(
        _selectedDate, allPrayerLogs, allPrayerTimes
    ) { date, logs, times ->
        val calculated = EgyptianPrayerCalculator.calculateTimes(date)
        val logMap = logs.filter { it.date == date }.associate { it.prayerId to it.status }
        val timeMap = times.filter { it.date == date }.associate { it.prayerId to it.customTime }

        EgyptianPrayerCalculator.PRAYERS_LIST.map { (id, name) ->
            val defaultTime = when (id) {
                "fajr" -> calculated.fajr
                "dhuhr" -> calculated.dhuhr
                "asr" -> calculated.asr
                "maghrib" -> calculated.maghrib
                "isha" -> calculated.isha
                else -> "12:00"
            }
            val custom = timeMap[id] ?: defaultTime
            val status = logMap[id] ?: ""
            PrayerInfo(id, name, custom, status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        try {
            val d = LocalDate.parse(date)
            _currentMonthCursor.value = d.withDayOfMonth(1)
        } catch (_: Exception) {}
    }

    fun nextMonth() {
        _currentMonthCursor.value = _currentMonthCursor.value.plusMonths(1)
    }

    fun prevMonth() {
        _currentMonthCursor.value = _currentMonthCursor.value.minusMonths(1)
    }

    fun goToToday() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        _selectedDate.value = today
        _currentMonthCursor.value = LocalDate.now().withDayOfMonth(1)
    }

    // Task Actions
    fun addTask(
        name: String,
        date: String = _selectedDate.value,
        priority: String = "Normal",
        subject: String = "",
        notes: String = ""
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val task = TaskEntity(
                name = name.trim(),
                date = date,
                priority = priority,
                subject = subject.trim(),
                notes = notes.trim()
            )
            repository.insertTask(task)
        }
    }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            val newDone = !task.done
            val updated = task.copy(
                done = newDone,
                revised = if (!newDone) false else task.revised
            )
            repository.updateTask(updated)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun copyTaskToDates(task: TaskEntity, dates: Set<String>) {
        viewModelScope.launch {
            val newTasks = dates.map { d ->
                task.copy(
                    id = UUID.randomUUID().toString(),
                    date = d,
                    done = false,
                    revised = false,
                    createdAt = System.currentTimeMillis()
                )
            }
            repository.insertTasks(newTasks)
        }
    }

    fun moveTask(task: TaskEntity, newDate: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(date = newDate))
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
        }
    }

    // Notes Actions
    fun createNote(title: String = "New Note", body: String = ""): String {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            val note = NoteEntity(id = id, title = title, body = body, updated = System.currentTimeMillis())
            repository.insertNote(note)
        }
        return id
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.insertNote(note.copy(updated = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Prayer Actions
    fun cyclePrayerStatus(prayerId: String) {
        val date = _selectedDate.value
        val currentLogs = allPrayerLogs.value.filter { it.date == date }.associate { it.prayerId to it.status }
        val current = currentLogs[prayerId] ?: ""
        val nextStatus = when (current) {
            "" -> "done"
            "done" -> "missed"
            else -> "" // reset
        }
        viewModelScope.launch {
            repository.setPrayerStatus(date, prayerId, nextStatus)
        }
    }

    fun setPrayerCustomTime(prayerId: String, time: String) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.setCustomPrayerTime(date, prayerId, time)
            if (notificationsEnabled.value && time.isNotBlank()) {
                val prayerName = EgyptianPrayerCalculator.PRAYERS_LIST.find { it.first == prayerId }?.second ?: prayerId
                NotificationScheduler.schedulePrayerReminder(getApplication(), prayerName, time)
            }
        }
    }

    // Settings & Theme
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefsRepository.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                // Schedule next prayers
                prayerListForSelectedDate.value.forEach { p ->
                    NotificationScheduler.schedulePrayerReminder(getApplication(), p.name, p.time)
                }
            }
        }
    }

    // Editable Labels
    fun setLabel(key: String, value: String) {
        viewModelScope.launch {
            repository.updateLabel(key, value)
        }
    }

    private fun calculateStreak(tasks: List<TaskEntity>): Int {
        val doneDays = tasks.filter { it.done }.map { it.date }.toSet()
        var d = LocalDate.now()
        val todayStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (!doneDays.contains(todayStr)) {
            d = d.minusDays(1)
            val yesterdayStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
            if (!doneDays.contains(yesterdayStr)) return 0
        }
        var streak = 0
        while (doneDays.contains(d.format(DateTimeFormatter.ISO_LOCAL_DATE))) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }

    private fun calculateBestStreak(tasks: List<TaskEntity>): Int {
        val doneDays = tasks.filter { it.done }.map { it.date }.toSet().sorted()
        if (doneDays.isEmpty()) return 0
        var best = 0
        var current = 0
        var prev: LocalDate? = null
        for (ds in doneDays) {
            val d = try { LocalDate.parse(ds) } catch (_: Exception) { null } ?: continue
            if (prev != null && prev.plusDays(1) == d) {
                current++
            } else {
                current = 1
            }
            if (current > best) best = current
            prev = d
        }
        return best
    }
}
