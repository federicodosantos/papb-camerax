package com.example.praktikumfirebase.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.praktikumfirebase.model.Player
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlayerViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance("https://praktikum-firebase-67c57-default-rtdb.asia-southeast1.firebasedatabase.app/").
    reference.child("players")

    private val _players = MutableLiveData<List<Player>>()
    val players: LiveData<List<Player>> = _players

    init {
        fetchPlayers()
    }

    fun fetchPlayers() {
        // Gunakan Firebase Realtime Database untuk mengambil data pemain
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedPlayers = mutableListOf<Player>()
                for (childSnapshot in snapshot.children) {
                    val player = childSnapshot.getValue(Player::class.java)
                    if (player != null) {
                        fetchedPlayers.add(player)
                    }
                }
                _players.postValue(fetchedPlayers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlayerViewModel", "Error fetching players: ${error.message}")
                // Bisa juga tambahkan error state ke UI
            }
        })
    }
}
