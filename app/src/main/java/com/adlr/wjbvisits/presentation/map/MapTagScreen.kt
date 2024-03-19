package com.adlr.wjbvisits.presentation.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adlr.wjbvisits.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("MissingPermission", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MapTagScreen(
    fusedLocationProviderClient: FusedLocationProviderClient,
    navController: NavController,
    visitId: String?,
    context: Context,
) {
    val scaffoldState = rememberScaffoldState()
    var currentLocation by remember { mutableStateOf(LocationUtils.getDefaultLocation()) }

    val cameraPositionState = rememberCameraPositionState()
    cameraPositionState.position = CameraPosition.fromLatLngZoom(
        LocationUtils.getPosition(currentLocation), 12f)

    var requestLocationUpdate by remember { mutableStateOf(true) }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            val titleText = "Check Location"
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
        MyGoogleMap(
            currentLocation,
            cameraPositionState,
            onGpsIconClick = {
                requestLocationUpdate = true
            },
            visitId = visitId,
            context = context
        )

        if (requestLocationUpdate) {
            LocationPermissionsAndSettingDialogs(
                updateCurrentLocation = {
                    requestLocationUpdate = false
                    LocationUtils.requestLocationResultCallback(fusedLocationProviderClient) { locationResult ->

                        locationResult.lastLocation?.let { location ->
                            currentLocation = location
                        }

                    }
                }
            )
        }
    }
}

@Composable
private fun MyGoogleMap(
    currentLocation: Location,
    cameraPositionState: CameraPositionState,
    onGpsIconClick: () -> Unit,
    visitId: String?,
    context: Context
) {

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(zoomControlsEnabled = false)
        )
    }

    val db = Firebase.firestore
    val remVisit: MutableState<DocumentSnapshot?> = remember { mutableStateOf(null) }

    LaunchedEffect(visitId) {
        db.collection("visits")
            .document(visitId!!)
            .get()
            .addOnSuccessListener { result ->
                remVisit.value = result
            }
            .addOnFailureListener { exception ->
                Log.d("VisitDetails", "failed to retrieve visit$exception")
            }
    }

    val isCheckIn = remVisit.value?.getBoolean("isCheckIn")
    val isCheckOut = remVisit.value?.getBoolean("isCheckOut")
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings
    ) {
        if (isCheckIn != null && isCheckIn != false && remVisit.value != null) {
            val locDb = remVisit.value!!.getString("checkInLocation")
            val locParts = locDb?.split(",")

            val latitude = locParts?.get(0)?.toDouble()
            val longitude = locParts?.get(1)?.toDouble()
            val location = LatLng(latitude!!, longitude!!)

            val timestamp: Timestamp = remVisit.value!!.getTimestamp("checkInTime")!!
            val date: Date = timestamp.toDate()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val formattedDate: String = sdf.format(date)

            Marker(
                state = MarkerState(position = location),
                title = "Check-In (${formattedDate})"
            )
        }
        if (isCheckOut != null && isCheckOut != false && remVisit.value != null) {
            val locDb = remVisit.value!!.getString("checkOutLocation")
            val locParts = locDb?.split(",")

            val latitude = locParts?.get(0)?.toDouble()
            val longitude = locParts?.get(1)?.toDouble()
            val location = LatLng(latitude!!, longitude!!)

            val timestamp: Timestamp = remVisit.value!!.getTimestamp("checkOutTime")!!
            val date: Date = timestamp.toDate()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val formattedDate: String = sdf.format(date)

            Marker(
                state = MarkerState(position = location),
                title = "Check-Out (${formattedDate})"
            )
        }
    }

    GpsIconButton(
        onIconClick = onGpsIconClick
    )
}

@Composable
private fun GpsIconButton(onIconClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onIconClick) {
                Icon(
                    modifier = Modifier.padding(bottom = 100.dp, end = 20.dp),
                    painter = painterResource(id = R.drawable.ic_gps_fixed),
                    contentDescription = null
                )
            }
        }
    }
}
