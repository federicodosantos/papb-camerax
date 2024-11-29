package com.example.praktikumfirebase.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.praktikumfirebase.model.Player
import com.example.praktikumfirebase.viewmodel.PlayerViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(
    currentUser: FirebaseUser?,
    onLogout: () -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: PlayerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Welcome, ${currentUser?.displayName ?: "User"}!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onLogout) {
                Text(text = "Sign Out")
            }
            Button(onClick = onNavigateToCreate) {
                Text(text = "Add Player")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        PlayerList(viewModel = viewModel)
    }
}

@Composable
fun PlayerItem(player: Player) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(16.dp)
        ) {
            AsyncImage(
                model = player.photoUrl,
                contentDescription = "Foto ${player.name}",
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop,
                onError = { state ->
                    Log.e("AsyncImage", "Full Error: ${state.result.throwable}")
                }
            )
            Column {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nomor Punggung: ${player.number}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = player.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun PlayerList(viewModel: PlayerViewModel) {
    val players by viewModel.players.observeAsState(emptyList())

    if (players.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No players available", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(players) { player ->
                PlayerItem(player = player)
            }
        }
    }
}
