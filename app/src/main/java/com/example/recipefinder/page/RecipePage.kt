package com.example.recipefinder.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recipefinder.R
import com.example.recipefinder.api.Meal
import com.example.recipefinder.firebase.AuthHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun RecipePage(navController: NavController, meal: Meal) {
    var imageVisible by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    var isFavorite by remember { mutableStateOf(false) }
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val db = FirebaseFirestore.getInstance()
    val firebaseManager = AuthHandler(LocalContext.current)

    LaunchedEffect(scrollState.value) {
        imageVisible = scrollState.value == 0
    }
    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("Favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("mealId", meal.idMeal)
                .get()
                .await()

            isFavorite = !snapshot.isEmpty
        }
    }

    val orangeColor = Color(0xFFFF8C00)
    val orangeGradient = Brush.verticalGradient(
        colors = listOf(orangeColor, orangeColor.copy(alpha = 0.7f))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(orangeGradient)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() },
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recipe Details",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(140.dp))
            IconButton(
                onClick = { isFavorite = !isFavorite
                    if (userId != null) {
                        if (isFavorite) {
                            meal.idMeal?.let {
                                firebaseManager.addFavorite(
                                    mealId = it,
                                    userId = userId,
                                    onSuccess = {},
                                    onFailure = {}
                                )
                            }
                        } else {
                            meal.idMeal?.let {
                                firebaseManager.removeFavorite(
                                    mealId = it,
                                    userId = userId,
                                    onSuccess = {},
                                    onFailure = {}
                                )
                            }
                        }
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.favorite_full_shape),
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        if (imageVisible) {
            Image(
                painter = rememberImagePainter(data = meal.strMealThumb),
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = meal.strMeal ?: "Unknown Recipe",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                color = orangeColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.strInstructions ?: "No instructions available.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                color = orangeColor
            )
            val ingredients = listOf(
                meal.strIngredient1 to meal.strMeasure1,
                meal.strIngredient2 to meal.strMeasure2,
                meal.strIngredient3 to meal.strMeasure3,
                meal.strIngredient4 to meal.strMeasure4,
                meal.strIngredient5 to meal.strMeasure5,
                meal.strIngredient6 to meal.strMeasure6,
                meal.strIngredient7 to meal.strMeasure7,
                meal.strIngredient8 to meal.strMeasure8,
                meal.strIngredient9 to meal.strMeasure9,
                meal.strIngredient10 to meal.strMeasure10,
                meal.strIngredient11 to meal.strMeasure11,
                meal.strIngredient12 to meal.strMeasure12,
                meal.strIngredient13 to meal.strMeasure13,
                meal.strIngredient14 to meal.strMeasure14,
                meal.strIngredient15 to meal.strMeasure15,
                meal.strIngredient16 to meal.strMeasure16,
                meal.strIngredient17 to meal.strMeasure17,
                meal.strIngredient18 to meal.strMeasure18,
                meal.strIngredient19 to meal.strMeasure19,
                meal.strIngredient20 to meal.strMeasure20
            ).filter { it.first != null && it.second != null }

            ingredients.forEach { (ingredient, measure) ->
                if (!ingredient.isNullOrEmpty())
                    Text(
                        text = "$ingredient: $measure",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
            }

            Spacer(modifier = Modifier.height(16.dp))
            CommentSection(mealId = meal.idMeal ?: "")
        }
    }
}

@Composable
fun CommentSection(mealId: String) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    var newRating by remember { mutableStateOf(0) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(mealId) {
        val snapshot = db.collection("Comments")
            .whereEqualTo("mealId", mealId)
            .get()
            .await()
        comments = snapshot.documents.map { it.toObject(Comment::class.java)!! }
    }

    val averageRating = if (comments.isNotEmpty()) {
        comments.map { it.rating }.average()
    } else 0.0

    val currentUser = auth.currentUser
    val userName = currentUser?.displayName ?: currentUser?.email ?: "Anonymous"
    val userId = currentUser?.uid ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.1f))
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Comment submission area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Comments: ${comments.size}")
            Text("Average Rating: ${"%.1f".format(averageRating)}")
        }

        TextField(
            value = newCommentText,
            onValueChange = { newCommentText = it },
            label = { Text("Add a comment") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i <= newRating) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { newRating = i }
                            .padding(2.dp)
                    )
                }
            }
            Text("Rating: $newRating")
        }

        Button(
            onClick = {
                val newComment = Comment(
                    id = "",
                    mealId = mealId,
                    text = newCommentText,
                    rating = newRating,
                    userId = userId,
                    userName = userName,
                    timestamp = System.currentTimeMillis()
                )

                db.collection("Comments")
                    .add(newComment)
                    .addOnSuccessListener { documentReference ->
                        comments = comments + newComment.copy(id = documentReference.id)
                        newCommentText = ""
                        newRating = 0
                    }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing comments
        comments.forEach { comment ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color.Black
                )
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color.DarkGray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    repeat(comment.rating) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700), // Darker yellow
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

data class Comment(
    val id: String = "",
    val mealId: String = "",
    val text: String = "",
    val rating: Int = 0,
    val userId: String = "",
    val userName: String = "",
    val timestamp: Long = 0
)
