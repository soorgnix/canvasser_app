package com.adlr.wjbvisits.presentation.customer

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adlr.wjbvisits.presentation.map.LocationPermissionsAndSettingDialogs
import com.adlr.wjbvisits.presentation.map.LocationUtils
import com.adlr.wjbvisits.presentation.sign_in.UserData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CustomerAdd(
    fusedLocationProviderClient: FusedLocationProviderClient,
    navController: NavController,
    modifier: Modifier = Modifier,
    userData: UserData?,
    context: Context,
) {
    val scaffoldState = rememberScaffoldState()

    val remNoteContent: MutableState<String?> = remember { mutableStateOf("") }
    val remAddressContent: MutableState<String?> = remember { mutableStateOf("") }
    val remNameContent: MutableState<String?> = remember { mutableStateOf("") }
    val remPICContent: MutableState<String?> = remember { mutableStateOf("") }
    val remPhoneContent: MutableState<String?> = remember { mutableStateOf("") }

    var currentLocation by remember { mutableStateOf(LocationUtils.getDefaultLocation()) }
    var requestLocationUpdate by remember { mutableStateOf(true) }
    LocationPermissionsAndSettingDialogs(
        updateCurrentLocation = {
            requestLocationUpdate = true
            LocationUtils.requestLocationResultCallback(fusedLocationProviderClient) { locationResult ->
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                }
            }
        }
    )

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (remAddressContent.value != "" && remNameContent.value != "")
                    {
                        val db = Firebase.firestore
                        val newRecord = CustomerModel(
                            address = remAddressContent.value!!,
                            name = remNameContent.value!!,
                            pic = remPICContent.value!!,
                            phone = remPhoneContent.value!!,
                            user_id = userData!!.email!!.lowercase(),
                            location = currentLocation.latitude.toString() + ", " + currentLocation.longitude.toString(),
                            isAdded = false,
                            addedDate = Timestamp.now(),
                            updateDate = Timestamp.now(),
                            note = remNoteContent.value!!
                        )
                        db.collection("customers")
                            .add(newRecord)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Saved",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed, try again",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        },
        topBar = {
            val titleText = "New Customer"
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                },
                backgroundColor = Color.Blue,
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(end = 32.dp)
        ) {
            Box(modifier = modifier) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(end = 32.dp)
                ) {
                    Text(text = ("Address"))
                    TextField(singleLine = false, maxLines = 4, modifier = Modifier.height(125.dp),
                        value = if (remAddressContent.value != null) {
                            remAddressContent.value!!
                        } else {
                            ""
                        },
                        onValueChange = { newValue: String ->
                            remAddressContent.value = newValue
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = ("Name"))
                    TextField(singleLine = true, maxLines = 1,
                        value = if (remNameContent.value != null) {
                            remNameContent.value!!
                        } else {
                            ""
                        },
                        onValueChange = { newValue: String ->
                            remNameContent.value = newValue
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = ("PIC"))
                    TextField(singleLine = true, maxLines = 1,
                        value = if (remPICContent.value != null) {
                            remPICContent.value!!
                        } else {
                            ""
                        },
                        onValueChange = { newValue: String ->
                            remPICContent.value = newValue
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = ("Phone"))
                    TextField(singleLine = true, maxLines = 1,
                        value = if (remPhoneContent.value != null) {
                            remPhoneContent.value!!
                        } else {
                            ""
                        },
                        onValueChange = { newValue: String ->
                            remPhoneContent.value = newValue
                        }
                    )
                    Text(text = ("Note"))
                    TextField(singleLine = false, maxLines = 4, modifier = Modifier.height(125.dp),
                        value = if (remNoteContent.value != null) {
                            remNoteContent.value!!
                        } else {
                            ""
                               },
                        onValueChange = { newValue: String ->
                            remNoteContent.value = newValue
                        }
                    )
                }
            }
        }
    }
}