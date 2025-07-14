package com.example.plantrecognizerui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.json.JSONObject
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(navController: NavHostController, label: String?, imageUri: String?) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val infoMap = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Încarcă json-ul o singură dată
    LaunchedEffect(Unit) {
        try {
            val inputStream = context.assets.open("plant_info.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val result = mutableMapOf<String, String>()
            jsonObject.keys().forEach { key ->
                result[key] = jsonObject.getString(key)
            }
            infoMap.value = result
        } catch (_: Exception) {
            infoMap.value = mapOf("error" to "Nu s-au putut încărca informațiile.")
        }
    }

    val description = infoMap.value[label] ?: "Informații indisponibile pentru această plantă."

    Scaffold(
        containerColor = Color(0xFFF6F6F6),
        contentWindowInsets = WindowInsets.systemBars, // ✅ suport margini sigure
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = label?.replaceFirstChar { it.uppercaseChar() } ?: "Plantă",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔍 Imaginea analizată
            if (!imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = Uri.parse(imageUri),
                    contentDescription = "Imagine analizată",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .background(Color.LightGray, shape = RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 📄 Descriere plantă
            Text(
                text = description,
                fontSize = 16.sp,
                color = Color.DarkGray,
                lineHeight = 24.sp
            )
        }
    }
}
