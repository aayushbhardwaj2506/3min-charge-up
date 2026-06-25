package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateSessionScript(
        breathing: String,
        mentalState: String,
        desiredOutcome: String,
        culturalInspiration: String,
        environmentPreference: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
            if (apiKey.isNullOrEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                Log.e(TAG, "Gemini API Key is missing or default. Falling back to bundled offline generator.")
                return@withContext getOfflineBundledScript(breathing, mentalState, desiredOutcome, culturalInspiration, environmentPreference)
            }

            val systemPrompt = """
                You are the 3-Minute Charge-Up Cinematic Mood Engine. 
                Your goal is to output a highly personalized, structured 3-minute cinematic emotional transformation script for a student.
                The transformation must guide them from their current breathing state '$breathing' and mental state '$mentalState' to their desired outcome '$desiredOutcome', using the cultural style of '$culturalInspiration' and environment of '$environmentPreference'.
                You must output ONLY a valid JSON object matching this schema. Do not output any markdown blocks, backticks, or 'json' headers.
                
                Schema:
                {
                  "themeDescription": "Detailed overall visual & audio ambiance description",
                  "backgroundAudioGenre": "Detailed description of the regional/tempo-matched instrumental style",
                  "segments": [
                    {
                      "segmentId": "intro",
                      "visualPrompt": "Detailed description of starting anime/stylized scene with environmental/cultural elements (0-30s)",
                      "quote": "Extremely short, powerful quote displayed on screen",
                      "narration": "Poetic, deep, supportive spoken/displayed caption guiding initial deep breaths",
                      "breathingPaceSeconds": 4, 
                      "musicTempo": "slow",
                      "primaryColor": "#12C2E9"
                    },
                    {
                      "segmentId": "buildup",
                      "visualPrompt": "Buildup scene, lighting shifting, visual motifs from cultural theme intensifying (30-90s)",
                      "quote": "Short dynamic quote on building strength",
                      "narration": "Uplifting narrator caption on moving past the obstacle",
                      "breathingPaceSeconds": 3,
                      "musicTempo": "moderate",
                      "primaryColor": "#C471ED"
                    },
                    {
                      "segmentId": "climax",
                      "visualPrompt": "Peak climax scene, grand landscape, massive explosion of beautiful cinematic energy, colors & light matching desired outcome (90-150s)",
                      "quote": "Peak emotional/motivational mantra",
                      "narration": "Powerful peak statement of empowerment, courage, or peaceful focus",
                      "breathingPaceSeconds": 0,
                      "musicTempo": "intense",
                      "primaryColor": "#F64F59"
                    },
                    {
                      "segmentId": "conclusion",
                      "visualPrompt": "Satisfying resolve scene, peaceful light settling, clear skies, clean environment, ready for challenges (150-180s)",
                      "quote": "Final focus-centric anchor quote",
                      "narration": "Closing grounding sentence. Breathe in the calm. You are ready.",
                      "breathingPaceSeconds": 5,
                      "musicTempo": "slow",
                      "primaryColor": "#00E676"
                    }
                  ]
                }
            """.trimIndent()

            val promptContent = """
                Generate an experience for:
                - Current Breathing: $breathing
                - Current Mental State: $mentalState
                - Desired Outcome: $desiredOutcome
                - Cultural Style/Regional Inspiration: $culturalInspiration
                - Environment Preference: $environmentPreference
            """.trimIndent()

            // Create Request Payload manually using standard JSON for maximum safety & zero serializing mismatch errors
            val contentsArray = JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", promptContent)
                ))
            )
            val systemInstruction = JSONObject().put("parts", JSONArray().put(
                JSONObject().put("text", systemPrompt)
            ))
            
            // Use JSON response format configuration
            val generationConfig = JSONObject()
                .put("responseMimeType", "application/json")
                .put("temperature", 0.75)

            val payload = JSONObject()
                .put("contents", contentsArray)
                .put("systemInstruction", systemInstruction)
                .put("generationConfig", generationConfig)

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed with code ${response.code}: $errBody")
                    return@withContext getOfflineBundledScript(breathing, mentalState, desiredOutcome, culturalInspiration, environmentPreference)
                }

                val resBody = response.body?.string() ?: ""
                val resJson = JSONObject(resBody)
                val candidates = resJson.getJSONArray("candidates")
                val textResponse = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Ensure it is valid JSON
                val cleanedText = cleanJsonString(textResponse)
                JSONObject(cleanedText) // Validate JSON parse
                return@withContext cleanedText
            }
        } catch (e: Throwable) {
            Log.e(TAG, "General error generating session script", e)
            return@withContext getOfflineBundledScript(breathing, mentalState, desiredOutcome, culturalInspiration, environmentPreference)
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.substring(7)
        } else if (str.startsWith("```")) {
            str = str.substring(3)
        }
        if (str.endsWith("```")) {
            str = str.substring(0, str.length - 3)
        }
        return str.trim()
    }

    fun getOfflineBundledScript(
        breathing: String,
        mentalState: String,
        desiredOutcome: String,
        culturalInspiration: String,
        environmentPreference: String
    ): String {
        // High quality fallbacks for various cultures and states to feel fully premium offline
        val themeDesc = "Offline cinematic atmosphere adapting to $culturalInspiration in $environmentPreference with $desiredOutcome focus."
        
        val color1 = when (desiredOutcome.lowercase()) {
            "comfort", "stress relief", "calmness" -> "#12C2E9"
            "focus", "inspiration" -> "#00E676"
            else -> "#FF512F" // Comeback, motivation, adrenaline
        }
        val color2 = when (desiredOutcome.lowercase()) {
            "comfort", "stress relief", "calmness" -> "#C471ED"
            "focus", "inspiration" -> "#3A7BD5"
            else -> "#DD2476"
        }
        
        val segments = JSONArray()
            .put(JSONObject()
                .put("segmentId", "intro")
                .put("visualPrompt", "A misty morning in $environmentPreference, stylized in beautiful $culturalInspiration art style. Soft ambient outlines shimmer as the world gently wakes up.")
                .put("quote", "The next three minutes belong only to you.")
                .put("narration", "Close your eyes to the noise of the day. Breathe in slowly. Let the $culturalInspiration silence anchor your mind.")
                .put("breathingPaceSeconds", 4)
                .put("musicTempo", "slow")
                .put("primaryColor", color1)
            )
            .put(JSONObject()
                .put("segmentId", "buildup")
                .put("visualPrompt", "Rays of radiant light begin to pierce through the canopy. Traditional $culturalInspiration visual elements swirl gently in a fluid dance of rising energy.")
                .put("quote", "One focused moment changes everything.")
                .put("narration", "You have faced storms before, and you are standing here now. Feel the slow, steady build-up of your inner resolve.")
                .put("breathingPaceSeconds", 3)
                .put("musicTempo", "moderate")
                .put("primaryColor", color2)
            )
            .put(JSONObject()
                .put("segmentId", "climax")
                .put("visualPrompt", "A spectacular peak cinematic explosion of golden solar light and brilliant particles over $environmentPreference. Dynamic energy waves flow outward, reflecting pure $desiredOutcome.")
                .put("quote", "Your comeback starts right here.")
                .put("narration", "Let all doubt burn away. The obstacle is the path. You are capable of infinite focus, peace, and absolute success!")
                .put("breathingPaceSeconds", 0)
                .put("musicTempo", "intense")
                .put("primaryColor", "#FF512F")
            )
            .put(JSONObject()
                .put("segmentId", "conclusion")
                .put("visualPrompt", "A serene, crisp blue sky over $environmentPreference. Glints of sparkling light drift like fireflies. Absolute clarity and balanced calm settles.")
                .put("quote", "Keep moving forward.")
                .put("narration", "Bring this quiet power back to your desk. Open your eyes. Your task is waiting, and you are fully charged.")
                .put("breathingPaceSeconds", 5)
                .put("musicTempo", "slow")
                .put("primaryColor", "#00E676")
            )

        return JSONObject()
            .put("themeDescription", themeDesc)
            .put("backgroundAudioGenre", "Ambient pads fused with traditional $culturalInspiration acoustic elements, tailored to $desiredOutcome.")
            .put("segments", segments)
            .toString()
    }
}
