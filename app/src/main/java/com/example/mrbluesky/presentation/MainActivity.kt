package com.example.mrbluesky.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.example.mrbluesky.presentation.theme.MrBlueSkyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MrBlueSkyTheme {
                BostonMarathonApp()
            }
        }
    }
}

// Data class to hold user input
data class UserData(
    val age: Int = 35,
    val gender: String = "M",
    val hours: Int = 3,
    val minutes: Int = 30,
    val seconds: Int = 0
)

// Custom color palette for better visual hierarchy
object AppColors {
    val Primary = Color(0xFFD257FC)
    val PrimaryDark = Color(0xFF990099)
    val PrimaryLight = Color(0xFFCC66CC)
    val Success = Color(0xFF46A971)
    val Error = Color(0xFFD90000)
    val Surface = Color(0xFF222121)
    val SurfaceVariant = Color(0xFF393838)
    val OnSurface = Color.White
    val OnSurfaceSecondary = Color(0xFF9999CC)
}

@Composable
fun BostonMarathonApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        var currentScreen by remember { mutableStateOf("input") }
        var userData by remember { mutableStateOf(UserData()) }
        var resultText by remember { mutableStateOf("") }

        // Handle physical back button
        BackHandler(enabled = currentScreen == "result" || currentScreen == "loading") {
            currentScreen = "input"
        }

        Scaffold(
            timeText = {
                TimeText(
                    modifier = Modifier.alpha(0.9f)
                )
            },
            positionIndicator = {
                if (currentScreen == "input") {
                    PositionIndicator(
                        scalingLazyListState = rememberScalingLazyListState()
                    )
                }
            }
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(300)
            ) { screen ->
                when (screen) {
                    "input" -> {
                        InputScreen(
                            userData = userData,
                            onUserDataChange = { userData = it },
                            onSubmit = { currentScreen = "loading" }
                        )
                    }

                    "loading" -> {
                        LoadingScreen(
                            userData = userData,
                            onResult = { result ->
                                resultText = result
                                currentScreen = "result"
                            }
                        )
                    }

                    "result" -> {
                        ResultScreen(
                            resultText = resultText,
                            onReset = { currentScreen = "input" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InputScreen(
    userData: UserData,
    onUserDataChange: (UserData) -> Unit,
    onSubmit: () -> Unit
) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 1)

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        autoCentering = AutoCenteringParams(itemIndex = 1)
    ) {
        // Top spacer for better centering
        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Header with animation
        item {
            HeaderSection()
        }

        // Age Selection
        item {
            InputCard(title = "Age") {
                InlineNumberPicker(
                    value = userData.age,
                    onValueChange = { onUserDataChange(userData.copy(age = it)) },
                    range = 18..80,
                    label = "years"
                )
            }
        }

        // Gender Selection
        item {
            GenderSelector(
                selectedGender = userData.gender,
                onGenderChange = { onUserDataChange(userData.copy(gender = it)) }
            )
        }

        // Race Time
        item {
            TimeInputCard(
                hours = userData.hours,
                minutes = userData.minutes,
                seconds = userData.seconds,
                onTimeChange = { h, m, s ->
                    onUserDataChange(userData.copy(hours = h, minutes = m, seconds = s))
                }
            )
        }

        // Submit Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedSubmitButton(onClick = onSubmit)
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun HeaderSection() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            onClick = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            backgroundPainter = CardDefaults.cardBackgroundPainter(
                startBackgroundColor = AppColors.Surface,
                endBackgroundColor = AppColors.Surface
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "🏃",
                    fontSize = 24.sp,
                    modifier = Modifier.scale(scale)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Boston Marathon",
                    style = MaterialTheme.typography.title3.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = AppColors.Primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Qualification Checker",
                    style = MaterialTheme.typography.body2.copy(fontSize = 11.sp),
                    color = AppColors.OnSurfaceSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun InputCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.SurfaceVariant)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.caption3,
                    color = AppColors.OnSurfaceSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun InlineNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactChip(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            modifier = Modifier.size(32.dp),
            colors = ChipDefaults.chipColors(backgroundColor = AppColors.PrimaryDark),
            label = { Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        )

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.title2.copy(fontSize = 20.sp),
            color = AppColors.Primary,
            modifier = Modifier.widthIn(min = 40.dp),
            textAlign = TextAlign.Center
        )

        CompactChip(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            modifier = Modifier.size(32.dp),
            colors = ChipDefaults.chipColors(backgroundColor = AppColors.PrimaryDark),
            label = { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        )
    }
}

@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.SurfaceVariant)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.caption3,
                    color = AppColors.OnSurfaceSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactChip(
                            onClick = { onGenderChange("M") },
                            label = {
                                Text(
                                    "Male",
                                    fontSize = 11.sp,
                                    color = if (selectedGender == "M") Color.White else AppColors.OnSurfaceSecondary
                                )
                            },
                            modifier = Modifier.height(32.dp),
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (selectedGender == "M") AppColors.Primary else AppColors.Surface
                            )
                        )

                        CompactChip(
                            onClick = { onGenderChange("F") },
                            label = {
                                Text(
                                    "Female",
                                    fontSize = 11.sp,
                                    color = if (selectedGender == "F") Color.White else AppColors.OnSurfaceSecondary
                                )
                            },
                            modifier = Modifier.height(32.dp),
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (selectedGender == "F") AppColors.Primary else AppColors.Surface
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeInputCard(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onTimeChange: (Int, Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.SurfaceVariant)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Race Time",
                    style = MaterialTheme.typography.caption3,
                    color = AppColors.OnSurfaceSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TimeSegment(
                            value = hours,
                            label = "H",
                            onIncrease = { if (hours < 8) onTimeChange(hours + 1, minutes, seconds) },
                            onDecrease = { if (hours > 0) onTimeChange(hours - 1, minutes, seconds) }
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.title2,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        TimeSegment(
                            value = minutes,
                            label = "M",
                            onIncrease = {
                                val newMinutes = if (minutes >= 59) 0 else minutes + 1
                                onTimeChange(hours, newMinutes, seconds)
                            },
                            onDecrease = {
                                val newMinutes = if (minutes <= 0) 59 else minutes - 1
                                onTimeChange(hours, newMinutes, seconds)
                            }
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.title2,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        TimeSegment(
                            value = seconds,
                            label = "S",
                            onIncrease = {
                                val newSeconds = if (seconds >= 59) 0 else seconds + 1
                                onTimeChange(hours, minutes, newSeconds)
                            },
                            onDecrease = {
                                val newSeconds = if (seconds <= 0) 59 else seconds - 1
                                onTimeChange(hours, minutes, newSeconds)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSegment(
    value: Int,
    label: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompactChip(
            onClick = onIncrease,
            modifier = Modifier.size(width = 36.dp, height = 20.dp),
            colors = ChipDefaults.chipColors(
                backgroundColor = AppColors.PrimaryDark.copy(alpha = 0.5f)
            ),
            label = { Text("▲", fontSize = 10.sp) }
        )

        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.title2.copy(fontSize = 18.sp),
            color = AppColors.Primary
        )

        Text(
            text = label,
            style = MaterialTheme.typography.caption3.copy(fontSize = 9.sp),
            color = AppColors.OnSurfaceSecondary
        )

        CompactChip(
            onClick = onDecrease,
            modifier = Modifier.size(width = 36.dp, height = 20.dp),
            colors = ChipDefaults.chipColors(
                backgroundColor = AppColors.PrimaryDark.copy(alpha = 0.5f)
            ),
            label = { Text("▼", fontSize = 10.sp) }
        )
    }
}

@Composable
fun AnimatedSubmitButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppColors.Primary,
            contentColor = Color.White
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = "CHECK QUALIFICATION",
            style = MaterialTheme.typography.button.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun LoadingScreen(
    userData: UserData,
    onResult: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val progress = remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Animate progress while loading
        launch {
            while (progress.value < 0.9f) {
                delay(50)
                progress.value += 0.05f
            }
        }

        // Make API call
        val result = sendJsonRequest(
            userData.age.toString(),
            userData.gender,
            userData.hours.toString(),
            userData.minutes.toString(),
            userData.seconds.toString()
        )

        progress.value = 1f
        delay(500)

        try {
            val json = JSONObject(result)
            val message = json.getString("RESULT_MESSAGE_OUT")
            onResult(message)
        } catch (e: Exception) {
            onResult("ERROR: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progress.value,
                    modifier = Modifier.size(64.dp),
                    indicatorColor = AppColors.Primary,
                    trackColor = AppColors.SurfaceVariant,
                    strokeWidth = 6.dp
                )
                Text(
                    text = "${(progress.value * 100).toInt()}%",
                    style = MaterialTheme.typography.caption2,
                    color = AppColors.OnSurfaceSecondary
                )
            }

            Text(
                text = "Checking Qualification",
                style = MaterialTheme.typography.body1,
                color = AppColors.OnSurface
            )

            Text(
                text = "Please wait...",
                style = MaterialTheme.typography.caption3,
                color = AppColors.OnSurfaceSecondary
            )
        }
    }
}

@Composable
fun ResultScreen(
    resultText: String,
    onReset: () -> Unit
) {
    val isQualified = resultText.contains("QUALIFIED") && !resultText.contains("NOT")

    val infiniteTransition = rememberInfiniteTransition()
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            // Result icon with animation
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isQualified) animatedScale else 1f)
                    .background(
                        color = if (isQualified) AppColors.Success.copy(alpha = 0.2f)
                        else AppColors.Error.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isQualified) "✓" else "✗",
                    fontSize = 40.sp,
                    color = if (isQualified) AppColors.Success else AppColors.Error,
                    fontWeight = FontWeight.Bold
                )
            }

            // Result message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isQualified) "Congratulations!" else "Keep Training!",
                    style = MaterialTheme.typography.title2.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isQualified) AppColors.Success else AppColors.Error
                )

                Text(
                    text = if (isQualified)
                        "You qualify for\nBoston Marathon 2026"
                    else "You don't qualify yet for\nBoston Marathon 2026",
                    style = MaterialTheme.typography.body2.copy(fontSize = 12.sp),
                    color = AppColors.OnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Check again button
                Chip(
                    onClick = onReset,
                    label = {
                        Text(
                            text = "CHECK AGAIN",
                            style = MaterialTheme.typography.button.copy(fontSize = 11.sp)
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = AppColors.Primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                // Back button
                Chip(
                    onClick = onReset,
                    label = {
                        Text(
                            text = "← BACK",
                            style = MaterialTheme.typography.button.copy(fontSize = 11.sp)
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = AppColors.SurfaceVariant,
                        contentColor = AppColors.OnSurfaceSecondary
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }
    }
}

suspend fun sendJsonRequest(
    age: String,
    gender: String,
    hours: String,
    minutes: String,
    seconds: String
): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val json = """
        {
          "AGE_IN": ${age.toInt()},
          "GENDER_IN": "$gender",
          "HOURS_IN": ${hours.toInt()},
          "MINUTES_IN": ${minutes.toInt()},
          "SECONDS_IN": ${seconds.toInt()}
        }
    """.trimIndent()

    val mediaType = "application/json".toMediaType()
    val body = RequestBody.create(mediaType, json)

    val request = Request.Builder()
        .url("http://10.141.13.69:1897/Rocket-Build-25/BQM/1.4/INPUT-REQUEST")
        .put(body)
        .addHeader("Content-Type", "application/json")
        .build()

    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext "Error ${response.code}: ${response.message}"
            }
            return@withContext response.body?.string() ?: "No response"
        }
    } catch (e: Exception) {
        return@withContext "Exception: ${e.message}"
    }
}