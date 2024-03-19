package com.adlr.wjbvisits.presentation.customer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CustomerDetails(
    navController: NavController,
    modifier: Modifier = Modifier,
    context: Context,
    customerId: String?
) {
    val scaffoldState = rememberScaffoldState()
    val remCustomer: MutableState<DocumentSnapshot?> = remember { mutableStateOf(null) }
    val db = Firebase.firestore

    db.collection("customers")
        .document(customerId!!)
        .get()
        .addOnSuccessListener { result ->
            remCustomer.value = result
        }
        .addOnFailureListener { exception ->
            Log.d("CustomerDetails", "failed to retrieve customer$exception")
        }

    val remNoteContent: MutableState<String?> = remember { mutableStateOf("") }
    val remAddressContent: MutableState<String?> = remember { mutableStateOf("") }
    val remNameContent: MutableState<String?> = remember { mutableStateOf("") }
    val remPICContent: MutableState<String?> = remember { mutableStateOf("") }
    val remPhoneContent: MutableState<String?> = remember { mutableStateOf("") }

    LaunchedEffect(remCustomer.value)
    {
        remNoteContent.value = remCustomer.value?.getString("note")
        remAddressContent.value = remCustomer.value?.getString("address")
        remNameContent.value = remCustomer.value?.getString("name")
        remPICContent.value = remCustomer.value?.getString("pic")
        remPhoneContent.value = remCustomer.value?.getString("phone")
    }

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
                            note = remNoteContent.value!!,
                            updateDate = Timestamp.now()
                        )
                        db.collection("customers").document(customerId!!)
                            .update(
                                mapOf(
                                    "note" to newRecord.note,
                                    "name" to newRecord.name,
                                    "pic" to newRecord.pic,
                                    "phone" to newRecord.phone,
                                    "address" to newRecord.address,
                                    "updateDate" to newRecord.updateDate
                                )
                            )
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
                Icon(imageVector = Icons.Default.Done, contentDescription = "Edit")
            }
        },
        topBar = {
            val titleText = "Update Customer"
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
                    if (remCustomer.value?.getTimestamp("addedDate") != null) {
                        val timestamp: Timestamp = remCustomer.value?.getTimestamp("addedDate")!!
                        val date: Date = timestamp.toDate()
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        val formattedDate: String = sdf.format(date)
                        Text(
                            text = "Added Date: %s".format(
                                formattedDate
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (remCustomer.value?.getTimestamp("updateDate") != null) {
                        val timestamp: Timestamp = remCustomer.value?.getTimestamp("updateDate")!!
                        val date: Date = timestamp.toDate()
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        val formattedDate: String = sdf.format(date)
                        Text(
                            text = "Update Date: %s".format(
                                formattedDate
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
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