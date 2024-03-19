package com.adlr.wjbvisits.presentation.profile

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adlr.wjbvisits.presentation.sign_in.UserData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(
    context: Context,
    userData: UserData?,
    onSignOut: () -> Unit,
    onVisits: () -> Unit,
    onCustomers: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(userData?.profilePictureUrl != null) {
            AsyncImage(
                model = userData.profilePictureUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if(userData?.username != null) {
            Text(
                text = userData.username,
                textAlign = TextAlign.Center,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        var remMessage = remember { mutableStateOf("") }
        var remStatus = remember { mutableStateOf("") }

        val db = Firebase.firestore

        db.collection("users")
            .whereEqualTo("email", userData?.email?.lowercase())
            .whereEqualTo("deletedFlag", false)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("MainActivity", "signInResult:success " + document.getString("email"))
                    remMessage.value = "Welcome"
                    remStatus.value = "true"
                }
            }
            .addOnFailureListener { exception ->
                Log.d("MainActivity", "signInResult:failed " + userData?.email + " not registered" + exception)
                remMessage.value = "Sign-in failed, " + userData?.email + " was not registered"
                remStatus.value = "false"
            }

        if (remStatus != null && remMessage != null) {
            Text(
                text = remMessage.value,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (remStatus.value == "true") {
            Button(onClick = onVisits) {
                Text(text = "Visits")
            }
            Button(onClick = onCustomers) {
                Text(text = "New Customers")
            }

        }
        Button(onClick = onSignOut) {
            Text(text = "Sign out")
        }
    }
}