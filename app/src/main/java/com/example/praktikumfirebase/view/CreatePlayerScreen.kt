package com.example.praktikumfirebase.view

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Composable
fun CreatePlayerScreen(onPlayerCreated: () -> Unit) {
    // Inisialisasi Supabase Client
    val supabaseClient = remember {
        createSupabaseClient(
            supabaseUrl = "https://rcgkqlwngainerhfsdqu.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJjZ2txbHduZ2FpbmVyaGZzZHF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzIxODk1ODgsImV4cCI6MjA0Nzc2NTU4OH0.wr6YPGemb5v1JJGtk4cxNU_nv5F2Ox4k_F522yQOoUM"
        ) {
            install(Storage) {
                transferTimeout = 90.seconds
            }
        }
    }

    // Inisialisasi Firebase Database
    val database = FirebaseDatabase.getInstance("https://praktikum-firebase-67c57-default-rtdb.asia-southeast1.firebasedatabase.app/").
    reference.child("players")

    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    suspend fun uploadImageToSupabase(bitmap: Bitmap, filename: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val imageBytes = outputStream.toByteArray()

                val path = "images/$filename"
                val result = supabaseClient.storage.from("player-images").upload(path, imageBytes)
                if (result.path.isNotEmpty()) { // Pastikan path valid
                    val publicUrl = supabaseClient.storage.from("player-images").publicUrl(path)
                    Log.d("Supabase", "Public URL: $publicUrl")
                    publicUrl
                } else {
                    Log.e("Supabase", "Upload failed")
                    null
                }
            } catch (e: Exception) {
                Log.e("Supabase", "Error uploading image", e)
                null
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                val filename = "player_${UUID.randomUUID()}.jpg"
                CoroutineScope(Dispatchers.Main).launch {
                    val url = uploadImageToSupabase(bitmap, filename)
                    if (url != null) {
                        photoUri = Uri.parse(url) // Update state untuk preview
                        photoUrl = url
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = number,
            onValueChange = { if (it.all { char -> char.isDigit() }) number = it },
            label = { Text("Nomor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Preview Gambar
        photoUri?.let {
            Image(
                painter = rememberAsyncImagePainter(model = it),
                contentDescription = "Photo Preview",
                modifier = Modifier.size(150.dp).padding(8.dp)
            )
        }

        Button(onClick = { launcher.launch(null) }) {
            Text("Ambil Foto")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Log.d("CreatePlayerScreen", "Button clicked")
                if (photoUrl != null) {
                    Log.d("CreatePlayerScreen", "Photo URL is valid: $photoUrl")
                    val playerData = mapOf(
                        "name" to name,
                        "number" to number.toIntOrNull(),
                        "description" to description,
                        "photoUrl" to photoUrl
                    )
                    Log.d("CreatePlayerScreen", "Preparing to save data: $playerData")
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val newPlayerRef = database.push()
                            Log.d("Firebase", "Pushing data to Firebase")
                            newPlayerRef.setValue(playerData).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("Firebase", "Player saved: ${newPlayerRef.key}")
                                    onPlayerCreated()
                                } else {
                                    Log.e("Firebase", "Failed to save player: ${task.exception}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("CreatePlayerScreen", "Error saving to Firebase", e)
                        }
                    }
                } else {
                    Log.e("CreatePlayerScreen", "Photo URL is null, cannot save")
                }
            },
            enabled = name.isNotEmpty() && number.isNotEmpty() && description.isNotEmpty() && photoUrl != null
        ) {
            Text("Simpan")
        }
    }
}