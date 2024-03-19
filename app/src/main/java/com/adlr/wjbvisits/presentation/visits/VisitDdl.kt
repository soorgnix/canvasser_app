package com.adlr.wjbvisits.presentation.visits

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adlr.wjbvisits.presentation.sign_in.UserData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun VisitDdl(
    context: Context,
    userData: UserData?,
    navController: NavController,
    onVisits: () -> Unit,
    activity: Activity
) {
    val scaffoldState = rememberScaffoldState()
    val visitSearchViewModel: VisitSearchViewModel = viewModel()

    val dropDownOptions = remember { mutableStateOf(listOf<String>()) }
    val userAreaMap = remember { mutableMapOf<String, String>() }

    val mDatePickerDialog = DatePickerDialog(
        activity,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val currentDate = Date(year - 1900, month, dayOfMonth)
            visitSearchViewModel.setDateQuery(year, month, dayOfMonth, Timestamp(currentDate))
        },
        visitSearchViewModel.remYear.value,
        visitSearchViewModel.remMonth.value,
        visitSearchViewModel.remDay.value
    )

    var mExpanded by remember { mutableStateOf(false) }
    val mSelectedText = remember { mutableStateOf("") }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val normalCustomer = remember { mutableStateOf("") }
    val debtCustomer = remember { mutableStateOf("") }

    val db = Firebase.firestore
    LaunchedEffect(visitSearchViewModel.dateQuery.value, mSelectedText.value) {
        val dateFrom = visitSearchViewModel.dateQuery.value;
        val calendar = Calendar.getInstance()
        calendar.time = dateFrom.toDate()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val dateTo = com.google.firebase.Timestamp(calendar.time)

        db.collection("visits")
            .orderBy("date", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("date", dateFrom)
            .whereLessThan("date", dateTo)
            .get()
            .addOnSuccessListener { result ->
                Log.d("firestore", "success retrieve datas")
                val documents = result.documents.mapNotNull { it as? QueryDocumentSnapshot }

                val filteredDocuments = documents.filter { document ->
                    val email = userData?.email?.lowercase() == document.getString("user_id")
                    val emailWjb = document.getString("user_id") == "wjb.canvasser@gmail.com"
                    val isUnchecked = document.getBoolean("isCheckIn") == false && document.getBoolean("isCheckOut") == false
                    //isUnchecked && (email || emailWjb)
                    isUnchecked
                }
                val distinctValuesSet = HashSet<String>() // Set to store unique values
                for (document in filteredDocuments) {
                    val fieldValue = document.getString("area")
                    if (fieldValue != null) {
                        // Add the value to the set, which will automatically remove duplicates
                        distinctValuesSet.add(fieldValue)
                    }
                }
                for (area in distinctValuesSet)
                {
                    val distinctUserIdSet = HashSet<String>()
                    val distinctInArea = filteredDocuments.filter { document ->
                        val isMatchArea = document.getString("area")?.lowercase() == area.lowercase()
                        isMatchArea
                    }
                    for (document in distinctInArea)
                    {
                        val userid = document.getString("user_id")
                        userid?.let { distinctUserIdSet.add(it) }
                    }
                    var fullDdlItemText = ""
                    for (user in distinctUserIdSet)
                    {
                        if (fullDdlItemText != "") {
                            fullDdlItemText = "$fullDdlItemText,$user"
                        } else {
                            fullDdlItemText = user
                        }
                    }
                    userAreaMap[area] = "$area ($fullDdlItemText)"
                }
                val finalValues = mutableListOf("=All=")
                finalValues.addAll(distinctValuesSet.toList().sorted())
                dropDownOptions.value = finalValues.toList()

                val filteredDocumentsArea = filteredDocuments.filter { document ->
                    var isArea = document.getString("area") == mSelectedText.value || mSelectedText.value == "=All="
                    isArea
                }

                val debtNumber = filteredDocumentsArea.count { document ->
                    val isDebt = document.getString("raport")?.lowercase()?.contains("menunggak")
                    isDebt == true
                }
                normalCustomer.value = (filteredDocumentsArea.count() - debtNumber).toString()
                debtCustomer.value = debtNumber.toString()
            }
            .addOnFailureListener { exception ->
                Log.d("firestore", "failed retrieve datas")
            }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            val titleText = "Select Master List"
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
        content = {
            Column()
            {
                Row()
                {
                    // Adding a space of 100dp height
                    Spacer(modifier = Modifier.size(10.dp))
                    // Displaying the mDate value in the Text
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "${visitSearchViewModel.remDay.value}/${visitSearchViewModel.remMonth.value + 1}/${visitSearchViewModel.remYear.value}",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Row()
                {
                    OutlinedTextField(
                        value = mSelectedText.value,
                        onValueChange = { mSelectedText.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                // This value is used to assign to
                                // the DropDown the same width
                                mTextFieldSize = coordinates.size.toSize()
                            },
                        label = {Text("Pilih Area")},
                        trailingIcon = {
                            Icon(icon,"contentDescription",
                                Modifier.clickable { mExpanded = !mExpanded })
                        }
                    )

                    // Create a drop-down menu with list of cities,
                    // when clicked, set the Text Field text as the city selected
                    DropdownMenu(
                        expanded = mExpanded,
                        onDismissRequest = { mExpanded = false },
                        modifier = Modifier
                            .width(with(LocalDensity.current){mTextFieldSize.width.toDp()})
                    ) {
                        dropDownOptions.value.forEach { label ->
                            DropdownMenuItem(onClick = {
                                mSelectedText.value = label
                                mExpanded = false
                            }) {
                                if (label != "=All="){
                                    userAreaMap[label]?.let { it1 -> Text(text = it1) }
                                }
                                else {
                                    Text(text = label)
                                }
                            }
                        }
                    }
                }
                Row() {
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "Customer Berhutang: ${debtCustomer.value}",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Row() {
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "Customer Lancar: ${normalCustomer.value}",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Row() {
                    Button(onClick = {
                        navController.navigate("visits/${visitSearchViewModel.remYear.value}/${visitSearchViewModel.remMonth.value}/${visitSearchViewModel.remDay.value}/${mSelectedText.value}")
                    })
                    {
                        Text(text = "Select")
                    }
                }
            }
        }
    )
}



