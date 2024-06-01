package com.example.recipefinder

import com.example.recipefinder.page.LoginPage
import com.example.recipefinder.api.Meal
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import android.util.Log
import com.example.recipefinder.api.RetrofitClient
import com.google.firebase.FirebaseApp
import com.google.gson.JsonObject
import com.example.recipefinder.api.jsonObjectToMeal
import com.example.recipefinder.page.HomeScreen
import com.example.recipefinder.page.ProfilePage
import com.example.recipefinder.page.RecipePage
import com.example.recipefinder.page.UserViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        //makeRequest("")
        setContent {
            RecipeFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginPage(navController)
                        }
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("recipe") {
                            RecipePage(navController)
                        }
                        composable("profile") {
                            val userViewModel = UserViewModel()
                            ProfilePage(navController, userViewModel)
                        }
                    }
                }
            }
        }
    }


}

fun makeRequest(searchKey: String, callback: (List<Meal>?) -> Unit) {
    val call: Call<JsonObject> = RetrofitClient.instance.getData(searchKey)

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


