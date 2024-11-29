package com.example.praktikumfirebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.example.praktikumfirebase.view.CreatePlayerScreen
import com.example.praktikumfirebase.view.HomeScreen
import com.example.praktikumfirebase.view.LoginScreen
import com.example.praktikumfirebase.viewmodel.PlayerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        currentUser = auth.currentUser

        FirebaseDatabase.getInstance().setLogLevel(com.google.firebase.database.Logger.Level.DEBUG)

        setContent {
            val navController = rememberNavController()

            val playerViewModel: PlayerViewModel = viewModel()

            NavHost(
                navController = navController,
                startDestination = if (currentUser == null) "login" else "home"
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            currentUser = auth.currentUser
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                composable("home") {
                    HomeScreen(
                        currentUser = currentUser,
                        viewModel = playerViewModel,
                        onNavigateToCreate = {
                            navController.navigate("create")
                        },
                        onLogout = {
                            auth.signOut()
                            currentUser = null
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
                composable("create") {
                    CreatePlayerScreen(
                        onPlayerCreated = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
