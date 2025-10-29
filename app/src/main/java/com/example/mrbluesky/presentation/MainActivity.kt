package com.example.mrbluesky.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import com.example.mrbluesky.presentation.theme.MrBlueSkyTheme
import com.example.mrbluesky.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display but keep status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide only navigation bar, keep status bar visible
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

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
    var currentScreen by remember { mutableStateOf("input") }
    var userData by remember { mutableStateOf(UserData()) }
    var resultText by remember { mutableStateOf("") }

    // Handle physical back button
    BackHandler(enabled = currentScreen == "result" || currentScreen == "loading") {
        currentScreen = "input"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
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

@Composable
fun InputScreen(
    userData: UserData,
    onUserDataChange: (UserData) -> Unit,
    onSubmit: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Add top padding to replace the top bar
        Spacer(modifier = Modifier.height(16.dp))

        // Header Card with running image
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Running image
                Image(
                    painter = painterResource(id = R.drawable.running),
                    contentDescription = "Running",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Boston Marathon\nQualification Checker",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Check if your time qualifies for Boston 2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.OnSurfaceSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Age Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Age",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (userData.age > 18) onUserDataChange(userData.copy(age = userData.age - 1))
                        },
                        modifier = Modifier.size(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AppColors.Primary)
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Text(
                        text = "${userData.age} years",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppColors.Primary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(32.dp))

                    OutlinedButton(
                        onClick = {
                            if (userData.age < 80) onUserDataChange(userData.copy(age = userData.age + 1))
                        },
                        modifier = Modifier.size(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AppColors.Primary)
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Gender Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // First row: Male and Female
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        onClick = { onUserDataChange(userData.copy(gender = "M")) },
                        label = {
                            Text(
                                "Male",
                                color = if (userData.gender == "M") Color.White else AppColors.OnSurfaceSecondary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        selected = userData.gender == "M",
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AppColors.Surface,
                            selectedContainerColor = AppColors.Primary
                        )
                    )
                    FilterChip(
                        onClick = { onUserDataChange(userData.copy(gender = "F")) },
                        label = {
                            Text(
                                "Female",
                                color = if (userData.gender == "F") Color.White else AppColors.OnSurfaceSecondary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        selected = userData.gender == "F",
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AppColors.Surface,
                            selectedContainerColor = AppColors.Primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Second row: Non Binary (centered)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        onClick = { onUserDataChange(userData.copy(gender = "NB")) },
                        label = {
                            Text(
                                "Non Binary",
                                color = if (userData.gender == "NB") Color.White else AppColors.OnSurfaceSecondary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        selected = userData.gender == "NB",
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AppColors.Surface,
                            selectedContainerColor = AppColors.Primary
                        )
                    )
                }
            }
        }

        // Race Time - Simplified without total time display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Race Time",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Hours
                TimeInputRow(
                    label = "Hours",
                    value = userData.hours,
                    onValueChange = { onUserDataChange(userData.copy(hours = it)) },
                    range = 0..10
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Minutes
                TimeInputRow(
                    label = "Minutes",
                    value = userData.minutes,
                    onValueChange = { onUserDataChange(userData.copy(minutes = it)) },
                    range = 0..59
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seconds
                TimeInputRow(
                    label = "Seconds",
                    value = userData.seconds,
                    onValueChange = { onUserDataChange(userData.copy(seconds = it)) },
                    range = 0..59
                )
            }
        }

        // Submit Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Check Qualification",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // Bottom spacing
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TimeInputRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.OnSurfaceSecondary,
            modifier = Modifier.width(80.dp),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.width(16.dp))

        OutlinedButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            },
            modifier = Modifier.size(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AppColors.Primary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(AppColors.Primary)
            ),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.Primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        OutlinedButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            },
            modifier = Modifier.size(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AppColors.Primary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(AppColors.Primary)
            ),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoadingScreen(
    userData: UserData,
    onResult: (String) -> Unit
) {
    val progress = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userData) {
        // Simulate progress
        for (i in 1..99 step 5) {
            delay(50)
            progress.floatValue = i / 100f
        }

        // Make API call
        val result = sendJsonRequest(
            userData.age.toString(),
            userData.gender,
            userData.hours.toString(),
            userData.minutes.toString(),
            userData.seconds.toString()
        )

        progress.floatValue = 0.99f
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress.floatValue },
                    modifier = Modifier.size(100.dp),
                    color = AppColors.Primary,
                    trackColor = AppColors.SurfaceVariant,
                    strokeWidth = 8.dp
                )
                Text(
                    text = "${(progress.floatValue * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.OnSurfaceSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Checking Qualification",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Please wait while we verify your time...",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.OnSurfaceSecondary,
                textAlign = TextAlign.Center
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Result Icon
        Icon(
            imageVector = if (isQualified) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = if (isQualified) "Qualified" else "Not Qualified",
            modifier = Modifier
                .size(120.dp)
                .scale(if (isQualified) animatedScale else 1f),
            tint = if (isQualified) AppColors.Success else AppColors.Error
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Result Message
        Text(
            text = if (isQualified) "Congratulations!" else "Keep Training!",
            style = MaterialTheme.typography.headlineLarge,
            color = if (isQualified) AppColors.Success else AppColors.Error,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isQualified)
                    AppColors.Success.copy(alpha = 0.1f)
                else
                    AppColors.Error.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isQualified)
                        "You qualify for the\n2026 Boston Marathon!"
                    else "You do not qualify for the\n2026 Boston Marathon yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Action Button
        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Check Another Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
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
        .url("http://services-emea.skytap.com:9007/rocket-build25-elt/BQM/1.11/INPUT-REQUEST")
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