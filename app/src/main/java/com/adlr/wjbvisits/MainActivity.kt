package com.adlr.wjbvisits

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adlr.wjbvisits.presentation.customer.CustomerAdd
import com.adlr.wjbvisits.presentation.customer.CustomerDelete
import com.adlr.wjbvisits.presentation.customer.CustomerDetails
import com.adlr.wjbvisits.presentation.customer.CustomerScreen
import com.adlr.wjbvisits.presentation.map.MapCheckScreen
import com.adlr.wjbvisits.presentation.map.MapTagScreen
import com.adlr.wjbvisits.presentation.profile.ProfileScreen
import com.adlr.wjbvisits.presentation.sign_in.GoogleAuthUiClient
import com.adlr.wjbvisits.presentation.sign_in.SignInScreen
import com.adlr.wjbvisits.presentation.sign_in.SignInViewModel
import com.adlr.wjbvisits.presentation.sign_in.UserData
import com.adlr.wjbvisits.presentation.visits.VisitDdl
import com.adlr.wjbvisits.presentation.visits.VisitDetails
import com.adlr.wjbvisits.presentation.visits.VisitScreen
import com.adlr.wjbvisits.ui.theme.WjbvisitsTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(/*context=*/applicationContext)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        setContent {
            WjbvisitsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {
                        composable("sign_in") {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if(googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if(result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if(state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }

                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            val userData: UserData? = googleAuthUiClient.getSignedInUser()
                            ProfileScreen(
                                userData = userData,
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.popBackStack()
                                    }
                                },
                                onVisits = { navController.navigate("visitDdl")},
                                onCustomers = {navController.navigate("customers")},
                                context = applicationContext
                            )
                        }
                        composable("visitDdl") {
                            val userData: UserData? = googleAuthUiClient.getSignedInUser()
                            VisitDdl(
                                context = applicationContext,
                                userData = userData,
                                navController = navController,
                                activity = LocalContext.current as Activity,
                                onVisits = {navController.navigate("visits")})
                        }
                        composable("visits/{year}/{month}/{day}/{master}",
                            arguments = listOf(navArgument("master") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userData: UserData? = googleAuthUiClient.getSignedInUser()
                            val year : Int = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: 0
                            val month : Int = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 0
                            val day : Int = backStackEntry.arguments?.getString("day")?.toIntOrNull() ?: 0
                            val master : String = backStackEntry.arguments?.getString("master") ?: ""
                            VisitScreen(context = applicationContext, userData = userData, navController = navController, activity = LocalContext.current as Activity, queryYear = year, queryMonth = month, queryDay = day, master = master)
                        }
                        composable("visit_details_screen/{visitId}",
                            arguments = listOf(navArgument("visitId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val visitId = backStackEntry.arguments?.getString("visitId")
                            // Call VisitDetails composable passing visitId parameter
                            VisitDetails(navController = navController, visitId = visitId, context = applicationContext)
                        }
                        composable("maps/{visitId}/{checkIn}",
                            arguments = listOf(navArgument("visitId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userData: UserData? = googleAuthUiClient.getSignedInUser()
                            var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
                            val visitId = backStackEntry.arguments?.getString("visitId")
                            val isCheckIn = backStackEntry.arguments?.getString("checkIn") == "true"
                            MapCheckScreen(fusedLocationProviderClient, visitId = visitId, isCheckIn = isCheckIn, context = applicationContext, navController = navController, userId = userData?.email?.lowercase())
                        }
                        composable("tags/{longitude}/{latitude}",
                            arguments = listOf(
                                navArgument("longitude") { type = NavType.StringType },
                                navArgument("latitude") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val longitude = backStackEntry.arguments?.getString("longitude")
                            val latitude = backStackEntry.arguments?.getString("latitude")
                            val destinationUri = Uri.parse("google.navigation:q=$latitude,$longitude")
                            val mapIntent = Intent(Intent.ACTION_VIEW, destinationUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            mapIntent.resolveActivity(packageManager)?.let {
                                startActivity(mapIntent)
                            }
                        }
                        composable("customers") {
                            val userData: UserData? = googleAuthUiClient.getSignedInUser()
                            CustomerScreen(userData = userData, navController = navController, activity = LocalContext.current as Activity, context = applicationContext)
                        }
                        composable("customers/add") {
                            var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
                            val userData: UserData? = googleAuthUiClient.getSignedInUser()
                            CustomerAdd(fusedLocationProviderClient, navController = navController, userData = userData, context = applicationContext)
                        }
                        composable("customer_details_screen/{customerId}",
                            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val customerId = backStackEntry.arguments?.getString("customerId")
                            CustomerDetails(navController = navController, customerId = customerId, context = applicationContext)
                        }
                        composable("customer_delete/{customerId}",
                            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val customerId = backStackEntry.arguments?.getString("customerId")
                            CustomerDelete(navController = navController, customerId = customerId, context = applicationContext)
                        }

                    }
                }
            }
        }
    }
}