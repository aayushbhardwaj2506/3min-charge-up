package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChargeUpSession
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ChargeUpViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF08090E))
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(600)) togetherWith
                                        fadeOut(animationSpec = tween(600))
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                Screen.ONBOARDING -> OnboardingScreen(viewModel)
                                Screen.GENERATING -> GeneratingScreen(viewModel)
                                Screen.SESSION -> SessionScreen(viewModel)
                                Screen.HISTORY -> HistoryScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: ChargeUpViewModel) {
    val selectedBreathing by viewModel.selectedBreathing.collectAsState()
    val selectedMentalState by viewModel.selectedMentalState.collectAsState()
    val customMentalState by viewModel.customMentalState.collectAsState()
    val selectedOutcome by viewModel.selectedOutcome.collectAsState()
    val selectedCulture by viewModel.selectedCulturalInspiration.collectAsState()
    val selectedEnv by viewModel.selectedEnvironmentPreference.collectAsState()

    // Beautiful descriptive mappings for UI labels
    val breathingOptions = listOf(
        "Calm" to "🌬️ Calm & Even",
        "Fast" to "⚡ Fast & Energetic",
        "Heavy" to "🌋 Heavy & Grounded",
        "Shallow" to "💨 Shallow & Light",
        "Irregular" to "🌊 Irregular / Flowing",
        "Not sure" to "❓ Unsure (Safe Calm)"
    )

    val mentalStates = listOf(
        "Lazy" to "🥱 Lazy / Sluggish",
        "Blank" to "😶 Blank / Numb",
        "Angry" to "😡 Angry / Frustrated",
        "Burned out" to "🥵 Burned Out",
        "Distracted" to "🌀 Distracted",
        "Nervous" to "😰 Nervous / Anxious",
        "Overwhelmed" to "🤯 Overwhelmed",
        "Sad" to "😢 Sad / Gloomy",
        "Lonely" to "👤 Lonely / Quiet",
        "Tired" to "😴 Tired / Sleepy",
        "Excited" to "🤩 Excited / Hype"
    )

    val outcomes = listOf(
        "Motivation" to "🔥 Pure Motivation",
        "Comeback energy" to "⚡ Comeback Power",
        "Confidence" to "🦁 Bold Confidence",
        "Comfort" to "🧸 Gentle Comfort",
        "Stress relief" to "🍃 Stress Relief",
        "Focus" to "🎯 Sharp Focus",
        "Courage" to "🛡️ Brave Courage",
        "Deadline push" to "⏰ Deadline Hustle",
        "Adrenaline rush" to "🚀 Adrenaline High"
    )

    val cultures = listOf(
        "Japanese" to "🌸 Japanese Anime Vibe",
        "Tamil" to "🪕 South Indian (Tamil)",
        "Hindi" to "🥁 North Indian (Hindi)",
        "Korean" to "🎋 Korean Drama Vibe",
        "Malayalam" to "🌴 Kerala Vibe (Malayalam)",
        "Telugu" to "🎻 Telugu Cinema Style",
        "Bengali" to "🌾 Bengali Heritage Vibe",
        "Assamese" to "🍃 Assamese Traditional",
        "Himalayan" to "🏔️ Himalayan Calmness",
        "Ladakh" to "🛖 Ladakh Buddhist Vibe",
        "Jammu & Kashmir" to "❄️ Kashmiri Serenity",
        "Brazilian" to "🐆 Brazilian Samba Vibe",
        "Western Cinematic" to "🎬 Hollywood Cinematic",
        "Futuristic" to "🌌 Cyberpunk / Futuristic"
    )

    val envs = listOf(
        "Village" to "🏡 Cozy Village",
        "Small Town" to "🌆 Nostalgic Small Town",
        "City" to "🌃 Cyberpunk City",
        "Mountains" to "🏔️ Misty Mountains",
        "Coast" to "🌊 Sandy Ocean Coast",
        "Forest" to "🌲 Ancient Whispering Forest",
        "Desert" to "🏜️ Serene Golden Desert",
        "Surprise Me" to "🎁 Surprise Me!"
    )

    var currentStep by rememberSaveable { mutableStateOf(0) }
    val totalSteps = 5

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Japanese Anime Floating Sakura Petals & Dynamic Nebula background
        AnimeBackgroundAnimation()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Stepper Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Step ${currentStep + 1} of $totalSteps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB7C5), // Sweet sakura pink
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when(currentStep) {
                                0 -> "Physical Calibration"
                                1 -> "Emotional Vibe"
                                2 -> "Ultimate Goal"
                                3 -> "Ethnicity & Culture"
                                else -> "Scenery Atmosphere"
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo(Screen.HISTORY) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Text("History", color = Color(0xFFA0A5C0), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smoothly animated progress indicator bar
                val progressFraction = (currentStep + 1).toFloat() / totalSteps.toFloat()
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                    label = "step_progress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0x15FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFB7C5), Color(0xFF7C9DFF))
                                )
                            )
                    )
                }
            }

            // Central Dynamic Step Wizard
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width / 2 } + fadeIn(tween(300))) togetherWith
                                    (slideOutHorizontally { width -> -width / 2 } + fadeOut(tween(300)))
                        } else {
                            (slideInHorizontally { width -> -width / 2 } + fadeIn(tween(300))) togetherWith
                                    (slideOutHorizontally { width -> width / 2 } + fadeOut(tween(300)))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "step_wizard_transition",
                    modifier = Modifier.fillMaxSize()
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (step) {
                            0 -> {
                                CardSection(
                                    title = "1. How is your breathing? 🌬️",
                                    subtitle = "Anime pacing is centered in the breath. Select your starting rate so we can calibrate the music tempo and narration."
                                ) {
                                    FlowRow(spacing = 8.dp) {
                                        breathingOptions.forEach { pair ->
                                            val isSelected = selectedBreathing == pair.first
                                            ChipItem(
                                                label = pair.second,
                                                isSelected = isSelected,
                                                onClick = { viewModel.selectedBreathing.value = pair.first },
                                                testTag = "breathing_${pair.first}"
                                            )
                                        }
                                    }
                                }
                            }
                            1 -> {
                                CardSection(
                                    title = "2. Current Mental State 🧠",
                                    subtitle = "How do you feel in your mind right now? This determines the atmospheric tone, depth of the guide, and dynamic visual color palette."
                                ) {
                                    FlowRow(spacing = 8.dp) {
                                        mentalStates.forEach { pair ->
                                            val isSelected = selectedMentalState == pair.first && customMentalState.isBlank()
                                            ChipItem(
                                                label = pair.second,
                                                isSelected = isSelected,
                                                onClick = {
                                                    viewModel.selectedMentalState.value = pair.first
                                                    viewModel.customMentalState.value = ""
                                                },
                                                testTag = "state_${pair.first}"
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = customMentalState,
                                        onValueChange = { viewModel.customMentalState.value = it },
                                        label = { Text("Or describe your unique feelings in your own words...") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFFB7C5),
                                            unfocusedBorderColor = Color(0x33FFFFFF),
                                            focusedLabelColor = Color(0xFFFFB7C5),
                                            unfocusedLabelColor = Color(0x66FFFFFF),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("custom_state_input"),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                            2 -> {
                                CardSection(
                                    title = "3. What do you need right now? ✨",
                                    subtitle = "What is the ultimate target of your emotional reset? This shapes the final quotes, advice climax, and peak music scales."
                                ) {
                                    FlowRow(spacing = 8.dp) {
                                        outcomes.forEach { pair ->
                                            val isSelected = selectedOutcome == pair.first
                                            ChipItem(
                                                label = pair.second,
                                                isSelected = isSelected,
                                                onClick = { viewModel.selectedOutcome.value = pair.first },
                                                testTag = "outcome_${pair.first}"
                                            )
                                        }
                                    }
                                }
                            }
                            3 -> {
                                CardSection(
                                    title = "4. Your Ethnicity & Cultural Inspiration 🌸",
                                    subtitle = "Select the ethnicity, cultural background, musical instrumentation, and regional visual style of your guide."
                                ) {
                                    FlowRow(spacing = 8.dp) {
                                        cultures.forEach { pair ->
                                            val isSelected = selectedCulture == pair.first
                                            ChipItem(
                                                label = pair.second,
                                                isSelected = isSelected,
                                                onClick = { viewModel.selectedCulturalInspiration.value = pair.first },
                                                testTag = "culture_${pair.first}"
                                            )
                                        }
                                    }
                                }
                            }
                            4 -> {
                                CardSection(
                                    title = "5. Environment Preference 🏔️",
                                    subtitle = "Choose the nature background scenery where your interactive cinematic animation takes place."
                                ) {
                                    FlowRow(spacing = 8.dp) {
                                        envs.forEach { pair ->
                                            val isSelected = selectedEnv == pair.first
                                            ChipItem(
                                                label = pair.second,
                                                isSelected = isSelected,
                                                onClick = { viewModel.selectedEnvironmentPreference.value = pair.first },
                                                testTag = "env_${pair.first}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stepper Navigation Control Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE608090E)) // Glassmorphic dark overlay
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button: animated in when step > 0
                    AnimatedVisibility(
                        visible = currentStep > 0,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        OutlinedButton(
                            onClick = { if (currentStep > 0) currentStep-- },
                            modifier = Modifier
                                .height(56.dp)
                                .weight(0.8f),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF))
                        ) {
                            Text("Back", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Next step or submit button
                    Button(
                        onClick = {
                            if (currentStep < totalSteps - 1) {
                                currentStep++
                            } else {
                                viewModel.startGeneratingExperience()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentStep == totalSteps - 1) Color(0xFFFFB7C5) else Color(0xFF7C9DFF)
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1.2f)
                            .testTag(if (currentStep == totalSteps - 1) "start_session_button" else "next_step_button"),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentStep == totalSteps - 1) "Start 3-Min Charge 🚀" else "Next Step ✨",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardSection(
    title: String,
    subtitle: String = "",
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x1F131124)), // Glassmorphic translucent dark amethyst
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFFA0A5C0),
                        lineHeight = 16.sp
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun ChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val transition = updateTransition(targetState = isSelected, label = "chip_state_transition")

    val backgroundColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 200) },
        label = "bg"
    ) { selected ->
        if (selected) Color(0xFF7C9DFF) else Color(0x12FFFFFF)
    }

    val borderColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 200) },
        label = "border"
    ) { selected ->
        if (selected) Color(0xFF7C9DFF) else Color(0x15FFFFFF)
    }

    val textColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 200) },
        label = "text"
    ) { selected ->
        if (selected) Color.Black else Color(0xFFE2E8F0)
    }

    val scale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 150) },
        label = "scale"
    ) { selected ->
        if (selected) 1.04f else 1.0f
    }

    Box(
        modifier = Modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FlowRow(
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    // Custom lightweight wrap layout implementation avoiding experimental APIs
    androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val lines = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentLine = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentLineWidth = 0
        val spacingPx = spacing.roundToPx()

        placeables.forEach { placeable ->
            if (currentLineWidth + placeable.width + spacingPx > constraints.maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = mutableListOf()
                currentLineWidth = 0
            }
            currentLine.add(placeable)
            currentLineWidth += placeable.width + spacingPx
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        var totalHeight = 0
        lines.forEachIndexed { index, line ->
            val maxLineHeight = line.maxOfOrNull { it.height } ?: 0
            totalHeight += maxLineHeight
            if (index < lines.size - 1) {
                totalHeight += spacingPx
            }
        }

        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            lines.forEach { line ->
                var x = 0
                val maxLineHeight = line.maxOfOrNull { it.height } ?: 0
                line.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + spacingPx
                }
                y += maxLineHeight + spacingPx
            }
        }
    }
}

@Composable
fun GeneratingScreen(viewModel: ChargeUpViewModel) {
    val loadingText by viewModel.generationLoadingText.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "generating")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08090E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated glowing energy orb
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .blur(20.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x3FFFDF82), Color(0x00FFDF82))
                        )
                    )
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Rotating outer ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0x667C9DFF),
                        startAngle = rotateAngle,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Pulsing inner nucleus
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFF64F59))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Preparing Your Movie",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = loadingText,
                fontSize = 14.sp,
                color = Color(0xFFA0A5C0),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("generation_status_text")
            )
        }
    }
}

@Composable
fun SessionScreen(viewModel: ChargeUpViewModel) {
    val script by viewModel.currentScript.collectAsState()
    val currentIndex by viewModel.currentSegmentIndex.collectAsState()
    val segmentTimeLeft by viewModel.currentSegmentTimeLeft.collectAsState()
    val totalTimeElapsed by viewModel.totalTimeElapsed.collectAsState()
    val isRunning by viewModel.isSessionRunning.collectAsState()

    val selectedCulture by viewModel.selectedCulturalInspiration.collectAsState()
    val selectedEnv by viewModel.selectedEnvironmentPreference.collectAsState()

    if (script == null || script?.segments.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7C9DFF))
        }
        return
    }

    val currentSegment = script!!.segments[currentIndex]

    // Breathing guidance system oscillator
    val breathingCycleSeconds = currentSegment.breathingPaceSeconds
    val breathingAnim = remember { Animatable(1f) }

    // Synchronized visual color mapping
    val segmentColor = remember(currentSegment.primaryColor) {
        try {
            Color(android.graphics.Color.parseColor(currentSegment.primaryColor))
        } catch (e: Exception) {
            Color(0xFF7C9DFF)
        }
    }

    // Launch breathing pacing animation loop
    LaunchedEffect(currentIndex, breathingCycleSeconds) {
        if (breathingCycleSeconds > 0) {
            while (true) {
                // Inhale (scale up)
                breathingAnim.animateTo(
                    targetValue = 1.7f,
                    animationSpec = tween(
                        durationMillis = (breathingCycleSeconds * 1000) / 2,
                        easing = FastOutSlowInEasing
                    )
                )
                delay(200) // Brief hold
                // Exhale (scale down)
                breathingAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(
                        durationMillis = (breathingCycleSeconds * 1000) / 2,
                        easing = FastOutSlowInEasing
                    )
                )
                delay(200) // Brief hold
            }
        } else {
            breathingAnim.snapTo(1.2f)
        }
    }

    // Interactive custom procedural canvas renderer
    val infiniteTransition = rememberInfiniteTransition(label = "scenery")
    val timeTick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scenery_time"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030305))
    ) {
        // Full screen custom scenery visualizer matching culture & environment preference
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background ambient gradients based on current segment primary color
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        segmentColor.copy(alpha = 0.18f),
                        Color(0xFF05050A)
                    )
                ),
                size = size
            )

            // Dynamic elements based on environment preference
            when (selectedEnv.lowercase()) {
                "mountains" -> {
                    // Draw beautiful peak mountains
                    val peak1 = Path().apply {
                        moveTo(0f, height)
                        lineTo(width * 0.35f, height * 0.45f + sin(timeTick) * 15f)
                        lineTo(width * 0.7f, height)
                        close()
                    }
                    drawPath(peak1, Color(0x3B1F2035))

                    val peak2 = Path().apply {
                        moveTo(width * 0.3f, height)
                        lineTo(width * 0.75f, height * 0.32f + cos(timeTick) * 10f)
                        lineTo(width, height)
                        close()
                    }
                    drawPath(peak2, Color(0x56151726))
                }
                "coast", "ocean", "beach" -> {
                    // Draw sea wave path
                    val wavePath = Path().apply {
                        moveTo(0f, height * 0.7f)
                        for (x in 0..width.toInt() step 20) {
                            val y = height * 0.75f + sin(x * 0.006f + timeTick * 2.5f) * 20f
                            lineTo(x.toFloat(), y)
                        }
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(wavePath, segmentColor.copy(alpha = 0.15f))
                }
                "forest" -> {
                    // Draw tall abstract forest outlines
                    for (i in 0..8) {
                        val treeX = width * (i / 8f)
                        val treeHeight = height * 0.55f + sin(timeTick + i) * 30f
                        drawRect(
                            color = Color(0x1F223D30),
                            topLeft = Offset(treeX - 15.dp.toPx(), treeHeight),
                            size = Size(30.dp.toPx(), height - treeHeight)
                        )
                    }
                }
                "desert" -> {
                    // Beautiful smooth sand dune curves
                    val dunePath = Path().apply {
                        moveTo(0f, height * 0.8f)
                        cubicTo(
                            width * 0.3f, height * 0.7f,
                            width * 0.6f, height * 0.9f,
                            width, height * 0.75f
                        )
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(dunePath, Color(0x2E423325))
                }
                "city" -> {
                    // Geometric stylized cityscape skyline
                    val buildingWidths = listOf(120f, 180f, 150f, 220f, 130f, 250f)
                    var curX = 0f
                    var idx = 0
                    while (curX < width) {
                        val bWidth = buildingWidths[idx % buildingWidths.size]
                        val bHeight = height * 0.5f + (idx % 3) * 60f + sin(timeTick) * 10f
                        drawRect(
                            color = Color(0x2A151624),
                            topLeft = Offset(curX, bHeight),
                            size = Size(bWidth, height - bHeight)
                        )
                        curX += bWidth + 10f
                        idx++
                    }
                }
                else -> {
                    // Deep cosmic space fields
                    drawCircle(
                        color = Color(0x0C7C9DFF),
                        radius = 200.dp.toPx(),
                        center = Offset(width * 0.5f, height * 0.4f + cos(timeTick) * 30f)
                    )
                }
            }

            // Draw dynamic particles / sparkles drifting across screen
            for (i in 0..12) {
                val sparkX = (width * 0.1f + (i * 145f + timeTick * 80f) % width)
                val sparkY = (height * 0.2f + (i * 220f + timeTick * 40f) % (height * 0.6f))
                val sparkAlpha = 0.3f + 0.6f * sin(timeTick * 1.5f + i).coerceIn(0f..1f)
                drawCircle(
                    color = Color.White.copy(alpha = sparkAlpha),
                    radius = (2 + (i % 3)).dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }

            // Draw regional cultural visual cues overlays
            when (selectedCulture.lowercase()) {
                "japanese" -> {
                    // Draw cherry blossom petal outlines drifting
                    for (i in 0..5) {
                        val petX = (width * 0.15f + (i * 200f + timeTick * 60f) % width)
                        val petY = (height * 0.1f + (i * 180f + timeTick * 110f) % height)
                        drawCircle(
                            color = Color(0x55FFC0CB),
                            radius = 6.dp.toPx(),
                            center = Offset(petX, petY)
                        )
                    }
                }
                "himalayan", "ladakh" -> {
                    // Render abstract prayer flags line dangling from the top
                    drawLine(
                        color = Color(0x26FFFFFF),
                        start = Offset(0f, 80.dp.toPx()),
                        end = Offset(width, 140.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    for (i in 1..6) {
                        val flagX = width * (i / 7f)
                        val flagY = 80.dp.toPx() + (140.dp.toPx() - 80.dp.toPx()) * (i / 7f)
                        val flagColors = listOf(Color.Blue, Color.White, Color.Red, Color.Green, Color.Yellow)
                        drawRect(
                            color = flagColors[i % flagColors.size].copy(alpha = 0.18f),
                            topLeft = Offset(flagX - 10.dp.toPx(), flagY),
                            size = Size(20.dp.toPx(), 24.dp.toPx())
                        )
                    }
                }
                "tamil", "telugu", "malayalam" -> {
                    // Traditional golden halo glow or arch lines
                    drawCircle(
                        color = Color(0x1BFFE082),
                        radius = 120.dp.toPx(),
                        center = Offset(width * 0.5f, height * 0.25f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                "futuristic" -> {
                    // Draw high tech glowing circuit coordinates or grid lines
                    for (i in 1..4) {
                        val lineY = height * 0.2f * i
                        drawLine(
                            color = Color(0x0E00E676),
                            start = Offset(0f, lineY),
                            end = Offset(width, lineY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }
        }

        // Top UI Controls and Progress indicators
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar and close button header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.ONBOARDING) },
                    modifier = Modifier.testTag("exit_session_button")
                ) {
                    Text("✕", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Cinematic linear total duration bar (3 mins = 180s total)
                val progressPercent = totalTimeElapsed.toFloat() / 180f
                LinearProgressIndicator(
                    progress = { progressPercent },
                    color = segmentColor,
                    trackColor = Color(0x22FFFFFF),
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Standard time formatting (3:00 countdown helper)
                val minutes = (180 - totalTimeElapsed) / 60
                val seconds = (180 - totalTimeElapsed) % 60
                Text(
                    text = String.format("%d:%02d", minutes, seconds),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle indicating current cultural mode
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x22000000))
                    .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${selectedCulture.uppercase()} CINEMATIC EXPERIENCE",
                    fontSize = 10.sp,
                    color = segmentColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            // Cinematic Quote Display Area with elegant visual spacing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Visual Quote
                    AnimatedContent(
                        targetState = currentSegment.quote,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(800)) togetherWith
                                    fadeOut(animationSpec = tween(800))
                        },
                        label = "quote_fade"
                    ) { quoteText ->
                        Text(
                            text = "\"$quoteText\"",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .testTag("session_quote_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Soft Voice Narration / Caption Overlay
                    AnimatedContent(
                        targetState = currentSegment.narration,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(1000)) togetherWith
                                    fadeOut(animationSpec = tween(1000))
                        },
                        label = "narration_fade"
                    ) { narrationText ->
                        Text(
                            text = narrationText,
                            fontSize = 15.sp,
                            color = Color(0xFFA0A5C0),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .testTag("session_narration_text")
                        )
                    }
                }
            }

            // Interactive Breathing Guidance Module at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (breathingCycleSeconds > 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(170.dp)
                        ) {
                            // Pulsing glowing breathing guide circle ring
                            Box(
                                modifier = Modifier
                                    .size(80.dp * breathingAnim.value)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                segmentColor.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Clean outer border circle matching tempo
                            Canvas(modifier = Modifier.size(95.dp)) {
                                drawCircle(
                                    color = segmentColor.copy(alpha = 0.6f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }

                            // Dynamic breathing text instruction
                            val breathingText = if (breathingAnim.value > 1.35f) {
                                "Inhale..."
                            } else if (breathingAnim.value < 1.15f) {
                                "Exhale..."
                            } else {
                                "Hold..."
                            }
                            Text(
                                text = breathingText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.testTag("breathing_instruction_text")
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Breathe with the circle",
                            fontSize = 12.sp,
                            color = Color(0x80FFFFFF)
                        )
                    }
                } else {
                    // High energy comeback/adrenaline phase (no slow breathing needed)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFF64F59), Color(0xFFFFD54F))
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Active",
                                tint = Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "CLIMAX PHASE: Feel the energy surge!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: ChargeUpViewModel) {
    val history by viewModel.sessionHistory.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08090E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ONBOARDING) },
                        modifier = Modifier.testTag("back_from_history")
                    ) {
                        Text("←", color = Color.White, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "History logs",
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )
                }

                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Text("Clear All", color = Color(0xFFF64F59), fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No history sessions yet",
                            color = Color(0xFF666A8A),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your completed emotional charge-ups will appear here.",
                            color = Color(0x3DFFFFFF),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(history) { session ->
                        HistoryCard(session) {
                            viewModel.loadHistoricalSession(session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(session: ChargeUpSession, onReplay: () -> Unit) {
    val dateStr = remember(session.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(session.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x12FFFFFF)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(20.dp))
            .clickable { onReplay() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color(0xFF7C9DFF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Goal: ${session.desiredOutcome}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Vibe: ${session.mentalState} • Style: ${session.culturalInspiration} • Env: ${session.environmentPreference}",
                    fontSize = 12.sp,
                    color = Color(0xFFA0A5C0)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C9DFF))
                    .clickable { onReplay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Replay Session",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AnimeBackgroundAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "anime_bg")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val petalCount = 18
    val petals = remember {
        List(petalCount) {
            MutablePetal(
                xPercent = (0..100).random() / 100f,
                yPercent = (0..100).random() / 100f,
                speedY = 0.03f + (0..100).random() / 3000f,
                speedX = -0.01f - (0..100).random() / 5000f,
                size = 6f + (0..120).random() / 10f,
                rotationSpeed = 0.4f + (0..100).random() / 100f,
                phase = (0..314).random() / 100f
            )
        }
    }

    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTime ->
                tick = frameTime
                petals.forEach { petal ->
                    petal.yPercent += petal.speedY
                    petal.xPercent += petal.speedX
                    petal.phase += 0.015f

                    if (petal.yPercent > 1.1f) {
                        petal.yPercent = -0.1f
                        petal.xPercent = (20..100).random() / 100f
                    }
                    if (petal.xPercent < -0.1f) {
                        petal.xPercent = 1.1f
                        petal.yPercent = (0..80).random() / 100f
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0B1E), Color(0xFF040408))
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val rad = size.width.coerceAtLeast(size.height) * 0.7f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x24FF8DA1), Color.Transparent),
                    center = Offset(
                        center.x + cos(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx(),
                        center.y + sin(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx()
                    ),
                    radius = rad
                ),
                radius = rad,
                center = Offset(
                    center.x + cos(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx(),
                    center.y + sin(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx()
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1FFF6B8B), Color.Transparent),
                    center = Offset(
                        center.x - cos(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx(),
                        center.y - sin(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx()
                    ),
                    radius = rad
                ),
                radius = rad,
                center = Offset(
                    center.x - cos(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx(),
                    center.y - sin(Math.toRadians(rotationAngle.toDouble())).toFloat() * 160.dp.toPx()
                )
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val triggerRedraw = tick

            petals.forEach { petal ->
                val x = petal.xPercent * canvasWidth + sin(petal.phase) * 15.dp.toPx()
                val y = petal.yPercent * canvasHeight
                val petalSize = petal.size.dp.toPx()
                val color = Color(0xFFFFB7C5)

                drawContext.canvas.save()
                drawContext.canvas.translate(x, y)
                drawContext.canvas.rotate(petal.phase * 40f + (petal.rotationSpeed * 8f))

                val path = Path().apply {
                    moveTo(0f, petalSize)
                    cubicTo(-petalSize * 0.6f, petalSize * 0.3f, -petalSize * 0.8f, -petalSize * 0.5f, 0f, -petalSize)
                    cubicTo(petalSize * 0.8f, -petalSize * 0.5f, petalSize * 0.6f, petalSize * 0.3f, 0f, petalSize)
                }

                drawPath(
                    path = path,
                    color = color,
                    alpha = 0.7f
                )

                drawPath(
                    path = path,
                    color = Color(0xFFFF8DA1),
                    style = Stroke(width = 0.8f.dp.toPx()),
                    alpha = 0.4f
                )

                drawContext.canvas.restore()
            }
        }
    }
}

class MutablePetal(
    var xPercent: Float,
    var yPercent: Float,
    var speedY: Float,
    var speedX: Float,
    var size: Float,
    var rotationSpeed: Float,
    var phase: Float
)
