package com.example.recipefinder


import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recipefinder.api.Meal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun FavouritePage(navController: NavController) {
    var favoriteMeals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val userId = user?.uid
    if (userId == null) {
        // Handle the case where user is not logged in
        Text("User not logged in", color = Color.Red)
        return
    }


    LaunchedEffect(user) {
        if (user != null) {
            val favoriteMealIds = db.collection("Favorites")
                .whereEqualTo("userId", user.uid)
                .get()
                .await()
                .documents.map { it.getString("mealId") ?: "" }

            if (favoriteMealIds.isNotEmpty()) {
                val mealList = mutableListOf<Meal>()
                favoriteMealIds.forEach { mealId ->
                    val mealSnapshot = db.collection("meals")
                        .document(mealId)
                        .get()
                        .await()

                    val meal = mealSnapshot.toObject(Meal::class.java)
                    if (meal != null) {
                        mealList.add(meal)
                    }
                }
                favoriteMeals = mealList
            } else {
                Log.d("FavouritePage", "No favorite meals found for user: ${user.uid}")
            }
        } else {
            Log.d("FavouritePage", "User is null")
        }
    }


    LazyColumn {
        items(favoriteMeals) { meal ->
            FavoriteRecipeItem(
                meal = meal,
                onClick = {
                navController.navigate("recipe/${meal.idMeal}")
            })
        }
    }
}

@Composable
fun FavoriteRecipeItem(meal: Meal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Yemek adı
            Text(
                text = meal.strMeal ?: "Unknown Recipe",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Yemek resmi
            Image(
                painter = rememberImagePainter(data = meal.strMealThumb),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(shape = RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
