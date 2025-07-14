package com.example.plantrecognizerui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.plantrecognizerui.classifier.PlantClassifier
import com.example.plantrecognizerui.classifier.classifyTopK
import com.example.plantrecognizerui.history.HistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@Composable
fun ResultScreen(navController: NavHostController, imageUri: String?, save: Boolean = true) {
    val context = LocalContext.current
    val classifier = remember { PlantClassifier(context) }
    var topLabels by remember { mutableStateOf(listOf<Pair<String, Float>>()) }
    var savedOnce by rememberSaveable(imageUri) { mutableStateOf(false) }

    LaunchedEffect(Pair(imageUri, save)) {
        imageUri?.let {
            try {
                val uri = Uri.parse(it)
                val inputStream = context.contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    topLabels = listOf("Imagine indisponibilă" to 0f)
                    return@LaunchedEffect
                }

                withContext(Dispatchers.Default) {
                    val results = classifier.classifyTopK(inputStream, topK = 3)
                    topLabels = results

                    if (save && !savedOnce && results.isNotEmpty()) {
                        savedOnce = true
                        val (label, score) = results.first()
                        HistoryManager.saveEntry(
                            HistoryManager.Entry(
                                id = UUID.randomUUID().toString(),
                                imageUri = it,
                                label = label,
                                confidence = score,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                topLabels = listOf("Eroare la analiză" to 0f)
            }
        }
    }

    Scaffold(containerColor = Color(0xFFF0F4F8),
        contentWindowInsets = WindowInsets.systemBars) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌿 Plant Identification Result",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.LightGray, Color.White)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                imageUri?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(it))
                            .error(android.R.drawable.ic_menu_report_image)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = topLabels.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    topLabels.forEachIndexed { index, (label, score) ->
                        Column {
                            Text(
                                text = "#${index + 1} ${label.replaceFirstChar { it.uppercase() }}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1565C0)
                            )
                            LinearProgressIndicator(
                                progress = score.coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color(0xFF4CAF50),
                                trackColor = Color(0xFFB0BEC5)
                            )
                            Text(
                                text = "Confidence: ${(score * 100).toInt()}%",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )

                            if (index == 0 && imageUri != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        navController.navigate("info/${label}/${Uri.encode(imageUri)}")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                                ) {
                                    Text("Learn more", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Back", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}