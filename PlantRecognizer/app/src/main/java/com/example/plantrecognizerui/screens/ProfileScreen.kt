package com.example.plantrecognizerui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
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
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val user = FirebaseAuth.getInstance().currentUser
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmare") },
            text = { Text("Ești sigur că vrei să ștergi contul? Această acțiune este ireversibilă.") },
            confirmButton = {
                TextButton(onClick = {
                    user?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController.navigate("auth") {
                                popUpTo("profile") { inclusive = true }
                            }
                        } else {
                            message = "❌ Eroare: ${task.exception?.localizedMessage}".also { showDeleteDialog = false }
                        }
                    }
                }) {
                    Text("Da")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anulează")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars, // ✅ adăugat pentru full screen
        topBar = {
            TopAppBar(
                title = { Text("Profilul Meu", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // ✅ respectă marginea de sus și jos
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AsyncImage(
                model = user?.photoUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Text(user?.email ?: "Fără email", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("UID: ${user?.uid}", fontSize = 12.sp, color = Color.Gray)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("🔐 Schimbă parola", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Noua parolă") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        message = "Parola trebuie să aibă minim 6 caractere."
                    } else {
                        user?.updatePassword(newPassword)
                            ?.addOnSuccessListener {
                                message = "✅ Parola a fost schimbată cu succes."
                                newPassword = ""
                            }
                            ?.addOnFailureListener {
                                message = "❌ Eroare: ${it.localizedMessage}"
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvează parola")
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (message.startsWith("✅")) Color(0xFF2E7D32) else Color.Red,
                    fontSize = 14.sp
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Șterge contul", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.White)
            }
        }
    }
}
