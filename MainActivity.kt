package com.detrim.ai

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DetrimApp()
                }
            }
        }
    }
}

@Composable
fun DetrimApp() {
    val context = LocalContext.current
    val quotaManager = remember { QuotaManager(context) }
    var remaining by remember { mutableStateOf(quotaManager.getRemaining()) }

    var selectedModel by remember { mutableStateOf(DetrimModel.FAST) }
    var selectedAspect by remember { mutableStateOf(AspectRatioOption.PORTRAIT_9_16) }
    var prompt by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var resultVideoUrl by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Detrim AI", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Осталось квот: $remaining / ${QuotaManager.TOTAL_QUOTA} (обнуление в 00:00)")

        Spacer(Modifier.height(20.dp))

        Text("Модель", fontWeight = FontWeight.SemiBold)
        Column(Modifier.fillMaxWidth()) {
            DetrimModel.values().forEach { model ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedModel == model,
                        onClick = { selectedModel = model }
                    )
                    Text("${model.displayName} — ${model.cost} квот")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Формат видео", fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth()) {
            AspectRatioOption.values().forEach { aspect ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    RadioButton(
                        selected = selectedAspect == aspect,
                        onClick = { selectedAspect = aspect }
                    )
                    Text(aspect.label)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Промпт (для вида)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                errorMessage = null
                val cost = selectedModel.cost
                if (!quotaManager.canAfford(cost)) {
                    errorMessage = "Недостаточно квот. Нужно $cost, доступно $remaining."
                    return@Button
                }
                isGenerating = true
                resultVideoUrl = null
                scope.launch {
                    // имитация "генерации"
                    delay((2500..5000).random().toLong())
                    val spent = quotaManager.spend(cost)
                    if (spent) {
                        remaining = quotaManager.getRemaining()
                        resultVideoUrl = VideoRepository.randomVideo(selectedAspect)
                    } else {
                        remaining = quotaManager.getRemaining()
                        errorMessage = "Недостаточно квот. Нужно $cost, доступно $remaining."
                    }
                    isGenerating = false
                }
            },
            enabled = !isGenerating,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isGenerating) "Генерация..." else "Сгенерировать видео")
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (isGenerating) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text("${selectedModel.displayName} создаёт видео...")
        }

        resultVideoUrl?.let { url ->
            Spacer(Modifier.height(24.dp))
            val isPortrait = selectedAspect == AspectRatioOption.PORTRAIT_9_16
            val boxModifier = if (isPortrait) {
                Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(9f / 16f)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            }
            Box(modifier = boxModifier) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setMediaController(MediaController(ctx).also { it.setAnchorView(this) })
                            setVideoURI(Uri.parse(url))
                            setOnPreparedListener {
                                it.isLooping = true
                                start()
                            }
                        }
                    },
                    update = { videoView ->
                        videoView.setVideoURI(Uri.parse(url))
                        videoView.start()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
