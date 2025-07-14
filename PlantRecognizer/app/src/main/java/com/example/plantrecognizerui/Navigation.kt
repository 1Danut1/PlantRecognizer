package com.example.plantrecognizerui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plantrecognizerapp.ui.screens.PlantSearchScreen
import com.example.plantrecognizerui.screens.AuthScreen
import com.example.plantrecognizerui.screens.HistoryScreen
import com.example.plantrecognizerui.screens.HomeScreen
import com.example.plantrecognizerui.screens.InfoScreen
import com.example.plantrecognizerui.screens.ProfileScreen
import com.example.plantrecognizerui.screens.ResultScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("result?uri={uri}&save={save}") { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri")
            val save = backStackEntry.arguments?.getString("save")?.toBooleanStrictOrNull() ?: true
            ResultScreen(navController, uri, save)
        }

        composable("info/{label}/{uri}") { backStackEntry ->
            val label = backStackEntry.arguments?.getString("label")
            val uri = backStackEntry.arguments?.getString("uri")
            InfoScreen(navController, label, uri)
        }

        composable("history") {
            HistoryScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("search") {
            PlantSearchScreen(navController)
        }


    }
}
