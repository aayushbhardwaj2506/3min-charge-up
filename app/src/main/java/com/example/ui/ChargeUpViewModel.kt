package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.api.SoundEngine
import com.example.data.AppDatabase
import com.example.data.ChargeUpSession
import com.example.data.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class Screen {
    ONBOARDING,
    GENERATING,
    SESSION,
    HISTORY
}

data class VisualSegment(
    val segmentId: String,
    val visualPrompt: String,
    val quote: String,
    val narration: String,
    val breathingPaceSeconds: Int,
    val musicTempo: String,
    val primaryColor: String
)

data class SessionScript(
    val themeDescription: String,
    val backgroundAudioGenre: String,
    val segments: List<VisualSegment>
)

class ChargeUpViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ChargeUpViewModel"
    private val repository: SessionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SessionRepository(database.sessionDao())
    }

    // List of historical sessions
    val sessionHistory: StateFlow<List<ChargeUpSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.ONBOARDING)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Form onboarding parameters
    val selectedBreathing = MutableStateFlow("Calm")
    val selectedMentalState = MutableStateFlow("Lazy")
    val customMentalState = MutableStateFlow("")
    val selectedOutcome = MutableStateFlow("Motivation")
    val selectedCulturalInspiration = MutableStateFlow("Japanese")
    val selectedEnvironmentPreference = MutableStateFlow("Mountains")

    // Experience generation state
    private val _generationLoadingText = MutableStateFlow("Synchronizing vibes...")
    val generationLoadingText = _generationLoadingText.asStateFlow()

    // Active session execution parameters
    private val _currentScript = MutableStateFlow<SessionScript?>(null)
    val currentScript = _currentScript.asStateFlow()

    private val _currentSegmentIndex = MutableStateFlow(0)
    val currentSegmentIndex = _currentSegmentIndex.asStateFlow()

    private val _currentSegmentTimeLeft = MutableStateFlow(45) // each segment is 45 seconds (4 * 45 = 180s)
    val currentSegmentTimeLeft = _currentSegmentTimeLeft.asStateFlow()

    private val _totalTimeElapsed = MutableStateFlow(0) // total running seconds from 0 to 180
    val totalTimeElapsed = _totalTimeElapsed.asStateFlow()

    private val _isSessionRunning = MutableStateFlow(false)
    val isSessionRunning = _isSessionRunning.asStateFlow()

    private var timerJob: Job? = null

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen != Screen.SESSION) {
            stopSession()
        }
    }

    fun startGeneratingExperience() {
        val breathingState = selectedBreathing.value
        val mood = if (customMentalState.value.isNotBlank()) customMentalState.value else selectedMentalState.value
        val outcome = selectedOutcome.value
        val culture = selectedCulturalInspiration.value
        val env = selectedEnvironmentPreference.value

        navigateTo(Screen.GENERATING)
        viewModelScope.launch {
            try {
                _generationLoadingText.value = "Calibrating mood algorithms..."
                delay(800)
                _generationLoadingText.value = "Synthesizing custom ${selectedCulturalInspiration.value} visuals..."
                delay(800)
                _generationLoadingText.value = "Composing atmospheric tones..."
                delay(800)

                _generationLoadingText.value = "Consulting AI Mood Engine..."
                val rawScriptJson = GeminiClient.generateSessionScript(
                    breathing = breathingState,
                    mentalState = mood,
                    desiredOutcome = outcome,
                    culturalInspiration = culture,
                    environmentPreference = env
                )

                // Parse the generated script
                val parsedScript = parseScriptJson(rawScriptJson)
                _currentScript.value = parsedScript

                // Save to database (wrapped in defensive try-catch so database errors never block session)
                try {
                    val sessionRecord = ChargeUpSession(
                        breathing = breathingState,
                        mentalState = mood,
                        desiredOutcome = outcome,
                        culturalInspiration = culture,
                        environmentPreference = env,
                        generatedScriptJson = rawScriptJson
                    )
                    repository.saveSession(sessionRecord)
                } catch (dbEx: Exception) {
                    Log.e(TAG, "Failed to save session record to database", dbEx)
                }

                // Start the experience screen
                startSession(parsedScript)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating session script, falling back to offline experience", e)
                try {
                    _generationLoadingText.value = "Initiating safe offline calibration..."
                    delay(500)
                    val rawScriptJson = GeminiClient.getOfflineBundledScript(
                        breathing = breathingState,
                        mentalState = mood,
                        desiredOutcome = outcome,
                        culturalInspiration = culture,
                        environmentPreference = env
                    )
                    val parsedScript = parseScriptJson(rawScriptJson)
                    _currentScript.value = parsedScript
                    startSession(parsedScript)
                } catch (fallbackEx: Exception) {
                    Log.e(TAG, "Severe fallback failure, returning to onboarding", fallbackEx)
                    _generationLoadingText.value = "Retrying calibration..."
                    delay(1000)
                    navigateTo(Screen.ONBOARDING)
                }
            }
        }
    }

    private fun parseScriptJson(jsonStr: String): SessionScript {
        val root = JSONObject(jsonStr)
        val themeDesc = root.optString("themeDescription", "Cinematic landscape")
        val backgroundAudio = root.optString("backgroundAudioGenre", "Ambient background sounds")
        
        val segmentsArray = root.getJSONArray("segments")
        val segmentsList = mutableListOf<VisualSegment>()
        
        for (i in 0 until segmentsArray.length()) {
            val sObj = segmentsArray.getJSONObject(i)
            segmentsList.add(
                VisualSegment(
                    segmentId = sObj.optString("segmentId", ""),
                    visualPrompt = sObj.optString("visualPrompt", ""),
                    quote = sObj.optString("quote", ""),
                    narration = sObj.optString("narration", ""),
                    breathingPaceSeconds = sObj.optInt("breathingPaceSeconds", 4),
                    musicTempo = sObj.optString("musicTempo", "slow"),
                    primaryColor = sObj.optString("primaryColor", "#FF512F")
                )
            )
        }
        return SessionScript(themeDesc, backgroundAudio, segmentsList)
    }

    private fun startSession(script: SessionScript) {
        _currentSegmentIndex.value = 0
        _currentSegmentTimeLeft.value = 45
        _totalTimeElapsed.value = 0
        _isSessionRunning.value = true
        navigateTo(Screen.SESSION)

        // Play Sound Engine
        val firstSegment = script.segments.firstOrNull()
        SoundEngine.start(
            tempo = firstSegment?.musicTempo ?: "slow",
            outcome = selectedOutcome.value
        )

        // Launch timer job
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isSessionRunning.value && _totalTimeElapsed.value < 180) {
                delay(1000)
                _totalTimeElapsed.value += 1
                
                val currentSegmentRemaining = _currentSegmentTimeLeft.value - 1
                if (currentSegmentRemaining <= 0) {
                    val nextIndex = _currentSegmentIndex.value + 1
                    if (nextIndex < script.segments.size) {
                        _currentSegmentIndex.value = nextIndex
                        _currentSegmentTimeLeft.value = 45
                        
                        // Update sound tempo dynamically based on segment progression
                        SoundEngine.updateTempo(script.segments[nextIndex].musicTempo)
                    } else {
                        // End of 3-minute sequence
                        break
                    }
                } else {
                    _currentSegmentTimeLeft.value = currentSegmentRemaining
                }
            }
            // Experience fully completed!
            _isSessionRunning.value = false
            SoundEngine.stop()
        }
    }

    fun stopSession() {
        _isSessionRunning.value = false
        timerJob?.cancel()
        timerJob = null
        SoundEngine.stop()
    }

    fun loadHistoricalSession(session: ChargeUpSession) {
        viewModelScope.launch {
            try {
                val parsedScript = parseScriptJson(session.generatedScriptJson)
                _currentScript.value = parsedScript
                selectedOutcome.value = session.desiredOutcome
                startSession(parsedScript)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading historical session", e)
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSession()
    }
}
