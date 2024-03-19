package com.adlr.wjbvisits.presentation.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MapCheckScreen(
    fusedLocationProviderClient: FusedLocationProviderClient,
    navController: NavController,
    visitId: String?,
    context: Context,
    isCheckIn: Boolean,
    userId: String?
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
            val titleText = if (isCheckIn) {"Check-In"} else {"Check-Out"}
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
            context = context,
            isCheckIn = isCheckIn,
            userId = userId
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
    context: Context,
    isCheckIn: Boolean,
    userId: String?
) {

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(zoomControlsEnabled = false)
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings
    ) {
        Marker(
            state = MarkerState(position = LocationUtils.getPosition(currentLocation)),
            title = "Current Position"
        )
    }

    GpsIconButton(
        onIconClick = onGpsIconClick,
        visitId = visitId!!,
        context = context,
        isCheckIn = isCheckIn,
        currentLocation = currentLocation,
        userId = userId!!
    )
}

@Composable
private fun GpsIconButton(onIconClick: () -> Unit, visitId: String, context: Context, isCheckIn:Boolean, currentLocation: Location, userId: String) {
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
                    imageVector = Icons.Default.Search,
                    contentDescription = "Details",
                    tint = MaterialTheme.colors.onSurface,
                )
            }

            IconButton(onClick = { onCheckInClick(visitId, context, isCheckIn, currentLocation, userId) }) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Details",
                    tint = MaterialTheme.colors.onSurface,
                )
            }
        }
    }
}

private fun onCheckInClick(visitId: String, context:Context, isCheckIn: Boolean, location: Location, userId: String)
{
    val db = Firebase.firestore

    if (isCheckIn) {
        db.collection("visits").document(visitId!!)
            .update(
                mapOf(
                    "user_id" to userId,
                    "isCheckIn" to true,
                    "checkInTime" to Timestamp.now(),
                    "checkInLocation" to (location.latitude.toString() + ", " + location.longitude.toString())
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
    }
    else
    {
        db.collection("visits").document(visitId!!)
            .update(
                mapOf(
                    "user_id" to userId,
                    "isCheckOut" to true,
                    "checkOutTime" to Timestamp.now(),
                    "checkOutLocation" to (location.latitude.toString() + ", " + location.longitude.toString())
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
    }
}

