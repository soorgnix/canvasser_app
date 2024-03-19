package com.adlr.wjbvisits.presentation.customer

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
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CustomerScreen(
    userData: UserData?,
    navController: NavController,
    activity: Activity,
    context: Context
) {
    val customerSearchViewModel: CustomerSearchViewModel = viewModel()
    val scaffoldState = rememberScaffoldState()
    val remCustomers: MutableState<List<QueryDocumentSnapshot>> = remember { mutableStateOf(emptyList()) }

    val filterOptions = listOf("all", "added", "not added")
    val db = Firebase.firestore

    LaunchedEffect(customerSearchViewModel.searchQuery.value, customerSearchViewModel.addedQuery.value) {
        db.collection("customers")
            .orderBy("address")
            .get()
            .addOnSuccessListener { result ->
                Log.d("firestore", "success retrieve datas")
                val documents = result.documents.mapNotNull { it as? QueryDocumentSnapshot }
                val filteredDocuments = documents.filter { document ->
                    val email = userData?.email?.lowercase() == document.getString("user_id")
                    val isAll = customerSearchViewModel.addedQuery.value == "all" || customerSearchViewModel.addedQuery.value == ""
                    val isAdded = customerSearchViewModel.addedQuery.value == "added" && document.getBoolean("isAdded") == true
                    val isNotAdded = customerSearchViewModel.addedQuery.value == "not added" && document.getBoolean("isAdded") == false
                    val isMatchSearch = customerSearchViewModel.searchQuery.value == "" || (customerSearchViewModel.searchQuery.value != "" && (
                            (document.getString("address") != "" && document.getString("address")!!.lowercase().contains(customerSearchViewModel.searchQuery.value.lowercase())) ||
                                    (document.getString("pic") != "" && document.getString("pic")!!.lowercase().contains(customerSearchViewModel.searchQuery.value.lowercase()))
                            ))
                    (isAll || isAdded || isNotAdded) && isMatchSearch && email
                }
                remCustomers.value = filteredDocuments
            }
            .addOnFailureListener { exception ->
                Log.d("firestore", "failed retrieve datas")
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("customers/add")
                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "+")
            }
        },
        scaffoldState = scaffoldState,
        topBar = {
            val titleText = "New Customers"
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
                        text = "Search       ",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    TextField(
                        singleLine = true,
                        maxLines = 1,
                        value = "${customerSearchViewModel.searchQuery.value}",
                        onValueChange = { newValue ->
                            customerSearchViewModel.setSearchQuery(newValue)
                        }
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row()
                {
                    filterOptions.forEach { option ->
                        RadioButton(
                            selected = (customerSearchViewModel.addedQuery.value == option),
                            onClick = { customerSearchViewModel.setAddedQuery(option) }
                        )
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp).align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(remCustomers.value.size) { index ->
                        Row()
                        {
                            CustomerItem(remCustomers.value[index], navController, Modifier, context = context)
                        }
                        Spacer(modifier = Modifier.size(5.dp))
                    }
                }
            }
        }
    )
}
