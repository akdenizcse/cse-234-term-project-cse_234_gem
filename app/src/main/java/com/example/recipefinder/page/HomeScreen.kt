@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.recipefinder.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recipefinder.Category
import com.example.recipefinder.MainActivity
import com.example.recipefinder.R
import com.example.recipefinder.api.Meal
import com.example.recipefinder.api.RetrofitClient
import com.example.recipefinder.fetchCategories
import com.example.recipefinder.firebase.AuthHandler
import com.example.recipefinder.makeRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController) {
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val user = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // Fetch recipes based on search query
    LaunchedEffect(searchQuery) {
        scope.launch {
            isLoading = true
            errorMessage = ""
            try {
                makeRequest(searchQuery, RetrofitClient.instance::getData) { receivedMeals ->
                    receivedMeals?.let {
                        meals = it
                    }
                }
            } catch (e: Exception) {
                errorMessage = "An error occurred while fetching recipes."
            } finally {
                isLoading = false
            }
        }
    }

    // Fetch categories
    LaunchedEffect(Unit) {
        fetchCategories(
            RetrofitClient.instance::getAllCategories,
            onSuccess = { receivedCategories ->
                receivedCategories?.let {
                    categories = it
                }
            },
            onFailure = {
                // Handle failure, if needed
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopBar(navController, searchQuery, onSearchQueryChange = { searchQuery = it })
        Spacer(modifier = Modifier.height(16.dp))

        // Display categories
        Column {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CategorySection(categories, selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = if (category == selectedCategory) null else category
                },
                onCategoryReset = { // Reset category selection
                    selectedCategory = null
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display either category name or "Popular Recipes"
        val displayText = if (selectedCategory != null) {
            selectedCategory!!.strCategory ?: ""
        } else {
            "Popular Recipes"
        }
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display recipes
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (meals.isEmpty()) {
            Text(
                text = "There is no recipe called \"$searchQuery\"",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            val filteredMeals = if (selectedCategory != null) {
                meals.filter { meal ->
                    meal.strCategory == selectedCategory?.strCategory
                }
            } else {
                meals
            }
            RecipeList(navController, filteredMeals, searchQuery, modifier = Modifier)
        }
    }
}




@Composable
fun TopBar(navController: NavController, searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discover, Cook, Delight\nYour Recipe Journey Starts Here!",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite),
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate("favorites") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                /*Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate("profile") }
                )*/
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFFF3F3F3), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Search any recipe") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search"
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}


@Composable
fun CategorySection(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    onCategoryReset: () -> Unit // Add this parameter for resetting category selection
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            CategoryItem(category, isSelected,
                onClick = {
                    if (!isSelected) {
                        onCategorySelected(category)
                    } else {
                        onCategoryReset()
                    }
                },
                onCategoryReset = onCategoryReset // Pass the onCategoryReset lambda
            )
        }
    }
}



@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCategoryReset: () -> Unit // Add this parameter for resetting category selection
) {
    Card(
        modifier = Modifier
            .size(100.dp) // Adjust the size as needed
            .clip(CircleShape)
            .clickable {
                if (isSelected) { // If category is already selected, reset selection
                    onCategoryReset()
                } else { // Otherwise, handle regular click
                    onClick()
                }
            },
        elevation = CardDefaults.cardElevation(
            if (isSelected) 8.dp else 0.dp
        ),
        shape = CircleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberImagePainter(data = category.strCategoryThumb),
                contentDescription = category.strCategory ?: "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp) // Adjust the size as needed
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.strCategory ?: "",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1, // Ensure text doesn't overflow
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}






@Composable
fun RecipeList(navController: NavController, meals: List<Meal>, query: String, modifier: Modifier) {
    if (meals.isEmpty()) {
        Text(
            text = "No recipe found for \"$query\"",
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(meals.size) { meal ->
                val currentMeal: Meal = meals[meal]
                RecipeItem(navController, currentMeal, modifier)
            }
        }
    }
}


@Composable
fun RecipeItem(navController: NavController, meal: Meal, modifier: Modifier = Modifier) {

    var isFavorite by remember { mutableStateOf(false) }
    var averageRating by remember { mutableStateOf(0.0) }
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val db = FirebaseFirestore.getInstance()
    val firebaseManager = AuthHandler(LocalContext.current)


    LaunchedEffect(meal.idMeal, userId) {
        if (userId != null && meal.idMeal != null) {
            val favoriteRef = db.collection("Favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("mealId", meal.idMeal)
                .get()
                .await()

            isFavorite = !favoriteRef.isEmpty
        }
    }


    LaunchedEffect(meal.idMeal) {
        if (meal.idMeal != null) {
            val commentsRef = db.collection("Comments")
                .whereEqualTo("mealId", meal.idMeal)
                .get()
                .await()

            val ratings = commentsRef.documents.mapNotNull { it.getDouble("rating") }
            if (ratings.isNotEmpty()) {
                averageRating = ratings.average()
            }
        }
    }

    Card(
        modifier = Modifier
            .clickable {
                MainActivity.staticMeal = meal
                navController.navigate("recipe")
            }
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                val painter = rememberImagePainter(data = meal.strMealThumb)

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = meal.strMeal.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f ★", averageRating),
                            color = Color.Gray
                        )
                    }
                }
            }
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
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
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.favorite_full_shape),
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFF4081) else Color.Gray
                )
            }
        }
    }
}

