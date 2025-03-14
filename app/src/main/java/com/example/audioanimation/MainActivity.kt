package com.example.audioanimation

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private var visualizer: Visualizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio)

        setContent {
            AudioAnimationScreen(mediaPlayer) { amplitude ->
                visualizer = Visualizer(mediaPlayer.audioSessionId).apply {
                    enabled = false
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                            waveform?.let {
                                val rms = it.map { it * it }.sum() / it.size
                                amplitude(rms.toFloat())
                            }
                        }
                        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
                    }, Visualizer.getMaxCaptureRate() / 2, true, false)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        visualizer?.release()
    }
}

@Composable
fun AudioAnimationScreen(mediaPlayer: MediaPlayer, setupVisualizer: (amplitude: (Float) -> Unit) -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var eyeSize by remember { mutableStateOf(30f) }
    var mouthWidth by remember { mutableStateOf(80f) }
    var amplitude by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            setupVisualizer { amp -> amplitude = amp }
            visualizer?.enabled = true
            while (isPlaying) {
                eyeSize = when {
                    amplitude > 1000 -> 50f
                    amplitude < 200 -> 20f
                    else -> 30f
                }
                mouthWidth = when {
                    amplitude > 1000 -> 100f
                    amplitude < 200 -> 60f
                    else -> 80f
                }
                delay(100)
            }
            visualizer?.enabled = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            drawCircle(
                color = Color(0xFF2F4F4F),
                radius = eyeSize,
                center = Offset(centerX - 40f, centerY - 30f)
            )
            drawCircle(
                color = Color(0xFF2F4F4F),
                radius = eyeSize,
                center = Offset(centerX + 40f, centerY - 30f)
            )
            drawLine(
                color = Color(0xFFFF6347),
                start = Offset(centerX - mouthWidth / 2, centerY + 30f),
                end = Offset(centerX + mouthWidth / 2, centerY + 30f),
                strokeWidth = 8f
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Button(
                onClick = {
                    if (!isPlaying) {
                        mediaPlayer.start()
                        isPlaying = true
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4682B4))
            ) {
                Text("Reproducir", color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4682B4))
            ) {
                Text("Pausar", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}