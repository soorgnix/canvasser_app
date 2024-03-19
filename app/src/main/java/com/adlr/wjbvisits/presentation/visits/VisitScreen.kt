package com.adlr.wjbvisits.presentation.visits

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adlr.wjbvisits.presentation.sign_in.UserData
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun VisitScreen(
    context: Context,
    userData: UserData?,
    navController: NavController,
    activity: Activity,
    queryYear: Int,
    queryMonth: Int,
    queryDay: Int,
    master: String
) {
    val visitSearchViewModel: VisitSearchViewModel = viewModel()

    val scaffoldState = rememberScaffoldState()
    val remVisits: MutableState<List<QueryDocumentSnapshot>> = remember { mutableStateOf(emptyList()) }

    //val mCalendar = Calendar.getInstance()
    //var remYear = remember { mutableStateOf(mCalendar.get(Calendar.YEAR))}
    //var remMonth = remember { mutableStateOf(mCalendar.get(Calendar.MONTH))}
    //var remDay = remember { mutableStateOf(mCalendar.get(Calendar.DAY_OF_MONTH))}

    //val defaultDate = Date(remYear.value - 1900, remMonth.value, remDay.value) // Note: year must be relative to 1900
    //visitSearchViewModel.setDateQuery(remYear.value, remMonth.value, remDay.value, com.google.firebase.Timestamp(defaultDate))
    //var remDate = remember { mutableStateOf(com.google.firebase.Timestamp(defaultDate))}

    val currentDate = Date(queryYear - 1900, queryMonth, queryDay)
    visitSearchViewModel.setDateQuery(queryYear, queryMonth, queryDay, com.google.firebase.Timestamp(currentDate))

    val filterOptions = listOf("Unchecked", "Checked-In", "Checked-Out", "Skip")
    val db = Firebase.firestore

    LaunchedEffect(visitSearchViewModel.dateQuery.value, visitSearchViewModel.checkedQuery.value, visitSearchViewModel.searchQuery.value) {
        val dateFrom = visitSearchViewModel.dateQuery.value;
        val calendar = Calendar.getInstance()
        calendar.time = dateFrom.toDate()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val dateTo = com.google.firebase.Timestamp(calendar.time)

        db.collection("visits")
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("user_id")
            .orderBy("area")
            .orderBy("master_list_order")
            .orderBy("address")
            .whereGreaterThanOrEqualTo("date", dateFrom)
            .whereLessThan("date", dateTo)
            .get()
            .addOnSuccessListener { result ->
                Log.d("firestore", "success retrieve datas")
                val documents = result.documents.mapNotNull { it as? QueryDocumentSnapshot }
                var filteredDocuments = documents.filter { document ->
                    val isMaster = document.getString("area")?.lowercase()?.contains(master.lowercase()) == true || master.lowercase() == "=all="
                    val email = userData?.email?.lowercase() == document.getString("user_id")
                    val emailWjb = document.getString("user_id") == "wjb.canvasser@gmail.com"
                    val isSkip = visitSearchViewModel.checkedQuery.value == "Skip" && document.getBoolean("isSkip") == true
                    val isCheckIn = visitSearchViewModel.checkedQuery.value == "Checked-In" && document.getBoolean("isCheckIn") == true && document.getBoolean("isCheckOut") != true && document.getBoolean("isSkip") != true
                    val isCheckOut = visitSearchViewModel.checkedQuery.value == "Checked-Out" && document.getBoolean("isCheckOut") == true  && document.getBoolean("isSkip") != true
                    val isUnchecked = visitSearchViewModel.checkedQuery.value == "Unchecked" && document.getBoolean("isCheckIn") != true && document.getBoolean("isCheckOut") != true  && document.getBoolean("isSkip") != true
                    val isMatchSearch = visitSearchViewModel.searchQuery.value == "" || (visitSearchViewModel.searchQuery.value != "" && (
                            (document.getString("area") != "" && document.getString("area")!!.lowercase().contains(visitSearchViewModel.searchQuery.value.lowercase())) ||
                                    (document.getString("address") != "" && document.getString("address")!!.lowercase().contains(visitSearchViewModel.searchQuery.value.lowercase())) ||
                                    (document.getString("daerah") != "" && document.getString("daerah")!!.lowercase().contains(visitSearchViewModel.searchQuery.value.lowercase())) ||
                                    (document.getString("pic") != "" && document.getString("pic")!!.lowercase().contains(visitSearchViewModel.searchQuery.value.lowercase()))
                            ))
                    //(isCheckIn || isCheckOut || isUnchecked || isSkip) && isMatchSearch && isMaster && (email || emailWjb)
                    (isCheckIn || isCheckOut || isUnchecked || isSkip) && isMatchSearch && isMaster
                }
                if (visitSearchViewModel.checkedQuery.value == "Checked-Out") {
                    filteredDocuments = filteredDocuments.sortedByDescending { document -> document.getTimestamp("checkInTime") }
                }
                remVisits.value = filteredDocuments
            }
            .addOnFailureListener { exception ->
                Log.d("firestore", "failed retrieve datas")
            }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            val titleText = "%s".format(master)
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
                backgroundColor = Color.Blue
            )
        },
        content =
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row()
                {
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "${visitSearchViewModel.remDay.value}/${visitSearchViewModel.remMonth.value + 1}/${visitSearchViewModel.remYear.value}",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Row()
                {
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "Search       ",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    TextField(
                        singleLine = true,
                        maxLines = 1,
                        value = "${visitSearchViewModel.searchQuery.value}",
                        onValueChange = { newValue ->
                            visitSearchViewModel.setSearchQuery(newValue)
                        }
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row()
                {
                    filterOptions.forEach { option ->
                        RadioButton(
                            selected = (visitSearchViewModel.checkedQuery.value == option),
                            onClick = { visitSearchViewModel.setCheckedQuery(option) }
                        )
                        Text(
                            text = option,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(start = 4.dp).align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(remVisits.value.size) { index ->
                        VisitItem(context, remVisits.value[index], navController, Modifier)
                    }
                }
            }
        })
}