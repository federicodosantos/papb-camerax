package com.example.praktikumfirebase.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.praktikumfirebase.R
import kotlinx.coroutines.launch
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 50.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))
        Row(modifier = Modifier.padding(top = 50.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.microsoft_logo),
                contentDescription = stringResource(id = R.string.desc_note_logo),
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp, start = 10.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.microsoft_one_note),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.login_instruction),
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                coroutineScope.launch {
                    try {
                        val result: GetCredentialResponse = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        handleSignIn(result, auth, onLoginSuccess)
                    } catch (e: GetCredentialException) {
                        Log.d("Error", e.message.toString())
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "logo google",
                    modifier = Modifier.size(38.dp)
                )
                Text(
                    text = "Sign in with Google",
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        }
    }
}

private fun handleSignIn(result: GetCredentialResponse, auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    when (val credential = result.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken, auth, onLoginSuccess)
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("LoginActivity", "Received an invalid google id token response", e)
                }
            } else {
                Log.e("LoginActivity", "Unexpected type of credential")
            }
        }
        else -> {
            Log.e("LoginActivity", "Unexpected type of credential")
        }
    }
}

private fun firebaseAuthWithGoogle(idToken: String, auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginActivity", "signInWithCredential:success")
                onLoginSuccess()
            } else {
                Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
            }
        }
}
