/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.mrbluesky.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.mrbluesky.R
import com.example.mrbluesky.presentation.theme.MrBlueSkyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType

class MainActivity : ComponentActivity() {

    private var showButton = mutableStateOf(true)
    private var resultText = mutableStateOf("Calculating...")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(
                showButton = showButton,
                resultText = resultText
            )
        }
    }
}

@Composable
fun WearApp(showButton: MutableState<Boolean>,
            resultText: MutableState<String>
) {
    MrBlueSkyTheme {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            ButtonJson(showButton = showButton,
                resultText = resultText)
        }
    }
}


@Composable
fun ButtonJson(showButton: MutableState<Boolean>,
               resultText: MutableState<String>) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (showButton.value) {
            Button(
                onClick = { SendJson(coroutineScope = coroutineScope,showButton = showButton,
                    resultText = resultText) },
                modifier = Modifier
                    .fillMaxWidth(0.8f) // optional width
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF990099), // Purple 500
                    contentColor = Color.White           // Text color
                )
            ) {
                Text("Check Qualification")
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (resultText.value == "Calculating...") {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = resultText.value,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1
                )
            }
        }

    }
}

suspend fun sendJsonRequest(): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val json = """
        {
          "AGE_IN": 36,
          "GENDER_IN": "F",
          "HOURS_IN": 2,
          "MINUTES_IN": 50,
          "SECONDS_IN": 0
        }
    """.trimIndent()

    val mediaType = "application/json".toMediaType()
    val body = RequestBody.create(mediaType, json)

    val request = Request.Builder()
        .url("http://10.141.13.213:1897/Rocket-Build-25/BQM/1.4/INPUT-REQUEST")
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

fun SendJson(coroutineScope: CoroutineScope,
             showButton: MutableState<Boolean>,
             resultText: MutableState<String>) {



    coroutineScope.launch {
        showButton.value = false

        delay(1000)
        val output = sendJsonRequest();

        val json = JSONObject(output)
        val message = json.getString("RESULT_MESSAGE_OUT")

        val displayMessage = when (message) {
            "QUALIFIED" -> "✅\nYou Are Qualified for Boston 2026 Marathon"
            "NOT QUALIFIED" -> "❌\nYou Are Not Qualified for Boston 2026 Marathon"
            else -> "❗ Error: Unexpected result \"$message\""
        }

        resultText.value = displayMessage
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ButtonJson(showButton = mutableStateOf(true),
        resultText = mutableStateOf("Calculating..."))
}