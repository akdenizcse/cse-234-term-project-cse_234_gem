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
                    if (responseBody.toString() == "{\"meals\":null}") {
                        Log.e("MainActivity", "Meals are null")
                        callback(emptyList()) // Return empty list if the meals are null
                    } else {
                        val meals: List<Meal> = jsonObjectToMeal(responseBody)
                        meals.forEachIndexed { index, meal ->
                            Log.d("MainActivity", "Meal ${index + 1}: $meal")
                        }
                        callback(meals)
                    }
                } else {
                    Log.e("MainActivity", "Response body is null")
                    callback(emptyList()) // Return empty list if response body is null
                }
            } else {
                Log.e("MainActivity", "Error: ${response.message()}")
                callback(null)
            }
        }

        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            Log.e("MainActivity", "Error: ${t.message}", t)
            callback(null)
        }
    })
}



data class Category(
    val idCategory: String,
    val strCategory: String?,
    val strCategoryThumb: String?,
    val strCategoryDescription: String?
)

fun fetchCategories(apiCall: () -> Call<JsonObject>, onSuccess: (List<Category>?) -> Unit, onFailure: () -> Unit) {
    val call: Call<JsonObject> = apiCall()

    call.enqueue(object : Callback<JsonObject> {
        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
            if (response.isSuccessful) {
                val responseBody: JsonObject? = response.body()
                if (responseBody != null && responseBody.has("categories")) {
                    val categoriesJsonArray = responseBody.getAsJsonArray("categories")
                    val categories = mutableListOf<Category>()

                    categoriesJsonArray?.forEach { categoryJson ->
                        val categoryObject = categoryJson.asJsonObject
                        val id = categoryObject.get("idCategory").asString
                        val name = categoryObject.get("strCategory").asString
                        val thumbUrl = categoryObject.get("strCategoryThumb").asString
                        val description = categoryObject.get("strCategoryDescription").asString

                        val category = Category(id, name, thumbUrl, description)
                        categories.add(category)
                    }

                    onSuccess(categories)
                } else {
                    onFailure()
                }
            } else {
                onFailure()
            }
        }

        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            onFailure()
        }
    })
}

