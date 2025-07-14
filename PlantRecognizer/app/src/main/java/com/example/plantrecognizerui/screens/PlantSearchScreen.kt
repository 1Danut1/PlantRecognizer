package com.example.plantrecognizerapp.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.WindowInsets

@Composable
fun PlantSearchScreen(navController: NavHostController) {
    val context = LocalContext.current
    val allPlants = remember { getAllPlantFolders(context) }
    var query by remember { mutableStateOf("") }
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var suggestions by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars // ✅ adăugat pentru suport margini moderne
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    suggestions = if (query.isNotBlank()) {
                        allPlants.filter { name ->
                            name.contains(query, ignoreCase = true)
                        }
                    } else emptyList()
                },
                label = { Text("Search plant") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(suggestions) { plantName ->
                    Text(
                        text = plantName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                query = plantName
                                suggestions = emptyList()
                                imageBitmap = findAndLoadAssetPlantImage(context, plantName)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Plant Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}

fun findAndLoadAssetPlantImage(context: Context, query: String): android.graphics.Bitmap? {
    val assetManager = context.assets
    val root = "plante"

    val folders = assetManager.list(root) ?: return null
    val matched = folders.find {
        it.equals(query.trim(), ignoreCase = true)
    } ?: return null

    val files = assetManager.list("$root/$matched") ?: return null
    val imageFile = files.firstOrNull { it.endsWith(".jpg") || it.endsWith(".png") } ?: return null

    val inputStream = assetManager.open("$root/$matched/$imageFile")
    return BitmapFactory.decodeStream(inputStream)
}

fun getAllPlantFolders(context: Context): List<String> {
    val assetManager = context.assets
    val root = "plante"
    return assetManager.list(root)?.toList() ?: emptyList()
}
