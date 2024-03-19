package com.adlr.wjbvisits.presentation.visits

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.adlr.wjbvisits.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun VisitDetails(
    context: Context,
    navController: NavController,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    cutCornerSize: Dp = 30.dp,
    visitId: String?,
) {
    val scaffoldState = rememberScaffoldState()
    val db = Firebase.firestore
    val remVisit: MutableState<DocumentSnapshot?> = remember { mutableStateOf(null) }

    db.collection("visits")
        .document(visitId!!)
        .get()
        .addOnSuccessListener { result ->
            remVisit.value = result
        }
        .addOnFailureListener { exception ->
            Log.d("VisitDetails", "failed to retrieve visit$exception")
        }

    val isCheckIn = remVisit.value?.getBoolean("isCheckIn")
    val isCheckOut = remVisit.value?.getBoolean("isCheckOut")
    val isSkip = remVisit.value?.getBoolean("isSkip")
    val remNoteContent: MutableState<String?> = remember { mutableStateOf("") }
    val currentNote = remVisit.value?.getString("note")
    remNoteContent.value = currentNote

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val location = remVisit.value?.getString("location")
                    if (!location.isNullOrEmpty()) {
                        val (longitude, latitude) = location.split(",").map { it.trim().toDouble() }
                        navController.navigate("tags/${latitude}/${longitude}")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "View in Maps")
            }
        },
        topBar = {
            val titleText = "Visit Details"
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
        var color = Color(ContextCompat.getColor(context, R.color.grey_cat))
        val isDebt = remVisit.value?.getString("raport")?.lowercase()?.contains("menunggak")
        val firstDayOfMonth = LocalDate.now().withDayOfMonth(1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        var isOrder = false
        val lastOrderDateString = remVisit.value?.getString("last_invoice")
        if (lastOrderDateString != null)
        {
            val date: Date = dateFormat.parse(lastOrderDateString)
            val lastOrderLocalDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            if (lastOrderLocalDate >= firstDayOfMonth)
            {
                isOrder = true
            }
        }

        if (isDebt == true && !isOrder)
        {
            color = Color(ContextCompat.getColor(context, R.color.bright_red_cat))
        }
        else if (isDebt != true && !isOrder)
        {
            color = Color(ContextCompat.getColor(context, R.color.yellow_cat))
        }
        else if (isDebt == true && isOrder)
        {
            color = Color(ContextCompat.getColor(context, R.color.blue_cat))
        }
        else if (isDebt != true && isOrder)
        {
            color = Color(ContextCompat.getColor(context, R.color.grey_cat))
        }
        if (isCheckIn == true && isCheckOut == true) {
            color = Color(ContextCompat.getColor(context, R.color.green_cat))
        } else if (isCheckIn == true) {
            color = Color(ContextCompat.getColor(context, R.color.red_cat))
        }
        if (isSkip == true) {
            color = Color(ContextCompat.getColor(context, R.color.skip_cat))
        }
        Column(
            modifier = Modifier
                .background(color = color)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row()
            {
                remVisit.value?.getString("customerCode")?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("pic")?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("master_list_order")?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("address")?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("daerah")?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("phone")?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row()
            {
                remVisit.value?.getString("last_invoice")?.let {
                    Text(
                        text = "Tgl Invoice\n %s \n".format(it),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("last_duedate")?.let {
                    Text(
                        text = "Tgl Jatuh Tempo\n %s \n".format(it),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("last_delivery")?.let {
                    Text(
                        text = "Tgl Kirim\n %s \n".format(it),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("last_payment")?.let {
                    Text(
                        text = "Cara Bayar\n %s \n".format(it),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                remVisit.value?.getString("last_complete")?.let {
                    Text(
                        text = "Tgl Selesai\n %s \n".format(it),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row()
            {
                Text(
                    text = "Pemesanan Terakhir",
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row()
            {
                remVisit.value?.getString("last_order")?.let {
                    Text(
                        text = "%s".format(it),
                        color = MaterialTheme.colors.onSurface,
                        overflow = TextOverflow.Visible,
                        style = TextStyle(fontSize = 12.sp)
                    )
                }
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row()
            {
                Text(
                    text = "Detil Pembayaran",
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row()
            {
                remVisit.value?.getString("payment_details")?.let {
                    Text(
                        text = "%s".format(it),
                        color = MaterialTheme.colors.onSurface,
                        overflow = TextOverflow.Visible,
                        style = TextStyle(fontSize = 12.sp)
                    )
                }
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row()
            {
                Text(
                    text = "Hutang",
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row()
            {
                remVisit.value?.getString("debt")?.let {
                    Text(
                        text = "%s".format(it),
                        color = MaterialTheme.colors.onSurface,
                        overflow = TextOverflow.Visible,
                        style = TextStyle(fontSize = 12.sp)
                    )
                }
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row()
            {
                remVisit.value?.getString("raport")?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row()
            {
                if (remVisit.value?.getBoolean("isCheckIn") == true) {
                    val timestamp: Timestamp = remVisit.value?.getTimestamp("checkInTime")!!
                    val date: Date = timestamp.toDate()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    val formattedDate: String = sdf.format(date)
                    Text(
                        text = "Check-In:\n %s \n (%s)".format(
                            formattedDate,
                            remVisit.value?.getString("checkInLocation")
                        ),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                if (remVisit.value?.getBoolean("isCheckOut") == true) {
                    val timestamp: Timestamp = remVisit.value?.getTimestamp("checkOutTime")!!
                    val date: Date = timestamp.toDate()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    val formattedDate: String = sdf.format(date)
                    Text(
                        text = "Check-Out:\n %s \n (%s)".format(
                            formattedDate,
                            remVisit.value?.getString("checkOutLocation")
                        ),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row()
            {
                TextField(
                    singleLine = false,
                    maxLines = 10,
                    value = if (remNoteContent.value != null) {
                        remNoteContent.value!!
                    } else {
                        ""
                    }, // Pass the value of remNoteContent using the .value property
                    onValueChange = { newValue: String ->
                        remNoteContent.value = newValue
                    }
                )
            }
            Row()
            {
                Text(text = "")
            }
            Row()
            {
                Divider(color = Color.Black, thickness = 1.dp)
            }
            Row(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                if (remVisit.value?.getBoolean("isSkip") != true) {
                    IconButton(
                        onClick = {
                            navController.navigate("maps/${visitId}/true")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Check-In",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
                if (remVisit.value?.getBoolean("isCheckIn") == true && remVisit.value?.getBoolean("isSkip") != true) {
                    IconButton(
                        onClick = {
                            navController.navigate("maps/${visitId}/false")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Check-Out",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
                IconButton(
                    onClick = {
                        val db = Firebase.firestore

                        db.collection("visits").document(visitId!!)
                            .update(
                                mapOf(
                                    "note" to remNoteContent.value // Fix: use remNoteContent.value instead of remNoteContent
                                )
                            ).addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Saved",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed, try again",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Save",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
                if (remVisit.value?.getBoolean("isSkip") != true) {
                    IconButton(
                        onClick = {
                            val db = Firebase.firestore

                            db.collection("visits").document(visitId!!)
                                .update(
                                    mapOf(
                                        "isSkip" to true,
                                        "skipTime" to Timestamp.now()
                                    )
                                ).addOnSuccessListener {
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
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Skip",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
                if (remVisit.value?.getBoolean("isSkip") == true) {
                    IconButton(
                        onClick = {
                            val db = Firebase.firestore

                            db.collection("visits").document(visitId!!)
                                .update(
                                    mapOf(
                                        "isSkip" to false,
                                        "skipTime" to ""
                                    )
                                ).addOnSuccessListener {
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
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "UnSkip",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
        }
    }
}