package com.example.recipefinder.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recipefinder.R
import com.example.recipefinder.api.Meal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun RecipePage(navController: NavController, meal: Meal) {
    var imageVisible by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.value) {
        imageVisible = scrollState.value == 0
    }

    val orangeColor = Color(0xFFFF9800)
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
                painter = painterResource(id = R.drawable.ic_back),
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
                color = Color.Gray
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
                        color = Color.Gray
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

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Comments: ${comments.size}")
            Text("Average Rating: ${"%.1f".format(averageRating)}")
        }

        comments.forEach { comment ->
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = "${comment.userName}: ${comment.text}")
                Text(text = "Rating: ${comment.rating}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = newCommentText,
            onValueChange = { newCommentText = it },
            label = { Text("Add a comment") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = newRating.toFloat(),
                onValueChange = { newRating = it.toInt() },
                valueRange = 0f..5f,
                steps = 4
            )
            Text("Rating: $newRating")
        }

        Button(onClick = {
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
        }) {
            Text("Submit")
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
