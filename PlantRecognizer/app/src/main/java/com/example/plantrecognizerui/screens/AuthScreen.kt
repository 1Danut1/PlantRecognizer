package com.example.plantrecognizerui.screens

import android.content.Context
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resetMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val googleSignInClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1067754302816-f7ncl31t9m8fnqdcbt4oqfl3n325qctn.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                } else {
                    errorMessage = task.exception?.message
                }
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage
        }
    }

    LaunchedEffect(Unit) {
        auth.currentUser?.let {
            navController.navigate("home") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        contentWindowInsets = WindowInsets.systemBars // ✅ permite padding corect pentru toate dispozitivele
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLogin) "Autentificare PlantRecognizer" else "Înregistrează un cont nou",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Parolă") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red)
            }

            Button(
                onClick = {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.length < 6) {
                        errorMessage = "Email invalid sau parolă prea scurtă."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        if (isLogin) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        navController.navigate("home") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = task.exception?.message
                                    }
                                }
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        navController.navigate("home") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = task.exception?.message
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLogin) "Autentificare" else "Înregistrare")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuă cu Google")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { isLogin = !isLogin }) {
                Text(if (isLogin) "Nu ai cont? Înregistrează-te" else "Ai deja cont? Autentifică-te")
            }

            TextButton(
                onClick = {
                    if (email.isNotEmpty()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                resetMessage = "📩 Email de resetare trimis la $email."
                            }
                            .addOnFailureListener {
                                resetMessage = "❌ Eroare: ${it.localizedMessage}"
                            }
                    } else {
                        resetMessage = "⚠️ Introdu mai întâi adresa de email."
                    }
                }
            ) {
                Text("Ai uitat parola?")
            }

            if (resetMessage.isNotEmpty()) {
                Text(
                    text = resetMessage,
                    color = when {
                        resetMessage.startsWith("📩") -> Color(0xFF2E7D32)
                        resetMessage.startsWith("⚠️") -> Color(0xFFFFA000)
                        else -> Color.Red
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
