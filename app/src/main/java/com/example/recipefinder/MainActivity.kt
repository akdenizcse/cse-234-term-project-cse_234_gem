package com.example.recipefinder

import UserViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipefinder.firebase.AuthHandler
import com.example.recipefinder.page.HomeScreen
import com.example.recipefinder.page.LoginPage
import com.example.recipefinder.page.ProfilePage
import com.example.recipefinder.page.RecipePage
import com.example.recipefinder.ui.theme.RecipeFinderTheme
//import com.example.recipefinder.ui.theme.com.example.recipefinder.ui.theme.Navigation.UserViewModel
import com.example.recipefinder.ui.theme.Navigation.UserViewModel
import com.google.firebase.FirebaseApp
import android.util.Log
import com.example.recipefinder.api.Meal
import com.example.recipefinder.api.RetrofitClient
import com.example.recipefinder.api.jsonObjectToMeal
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    companion object {
        var staticMeal: Meal? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            RecipeFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authHandler = AuthHandler(applicationContext)
                    val userViewModel = ViewModelProvider(this, UserViewModelFactory(authHandler)).get(
                        UserViewModel::class.java)
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginPage(navController)
                        }
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("profile") {
                            ProfilePage(navController, userViewModel)
                        }
                        composable("favorites") {
                            FavouritePage(navController)
                        }
                        composable("recipe") {
                            RecipePage(navController = navController, meal = staticMeal!!)
                        }

                    }
                }
            }
        }
    }
}

fun makeRequest(
    searchKey: String,
    apiCall: (String) -> Call<JsonObject>,
    callback: (List<Meal>?) -> Unit
) {
    val call: Call<JsonObject> = apiCall(searchKey)

    call.enqueue(object : Callback<JsonObject> {
        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
            if (response.isSuccessful) {
                val responseBody: JsonObject? = response.body()
                if (responseBody != null) {
                    // Convert JsonObject to List of Meals
                    val meals: List<Meal> = jsonObjectToMeal(responseBody)

                    // Log each meal object
                    meals.forEachIndexed { index, meal ->
                        Log.d("MainActivity", "Meal ${index + 1}: $meal")
                    }

                    // Call the callback with the list of meals
                    callback(meals)
                } else {
                    Log.e("MainActivity", "Response body is null")
                    // Call the callback with null indicating failure
                    callback(null)
                }
            } else {
                Log.e("MainActivity", "Error: ${response.message()}")
                // Call the callback with null indicating failure
                callback(null)
            }
        }

        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            Log.e("MainActivity", "Error: ${t.message}", t)
            // Call the callback with null indicating failure
            callback(null)
        }
    })
}
