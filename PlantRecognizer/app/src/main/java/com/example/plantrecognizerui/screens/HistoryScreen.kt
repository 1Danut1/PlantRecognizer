package com.example.plantrecognizerui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.plantrecognizerui.history.HistoryManager
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val entries = remember { mutableStateListOf<HistoryManager.Entry>() }

    LaunchedEffect(Unit) {
        HistoryManager.getHistory().collect {
            entries.clear()
            entries.addAll(it)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars, // ✅ asigură padding pentru margini sigure
        topBar = {
            TopAppBar(
                title = { Text("Prediction History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (entries.isEmpty()) {
                Text("No predictions yet.", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(entries) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("result?uri=${Uri.encode(entry.imageUri)}&save=false")
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = entry.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.LightGray)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text("Confidence: ${(entry.confidence * 100).toInt()}%", fontSize = 14.sp, color = Color.Gray)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        HistoryManager.deleteEntry(entry)
                                        entries.remove(entry)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
