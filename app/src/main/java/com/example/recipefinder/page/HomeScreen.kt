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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recipefinder.MainActivity
import com.example.recipefinder.R
import com.example.recipefinder.api.Meal
import com.example.recipefinder.makeRequest
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    var modifier = Modifier
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        scope.launch {
            isLoading = true
            errorMessage = ""
            try {
                makeRequest(searchQuery) { receivedMeals ->
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TopBar(navController, searchQuery, onSearchQueryChange = { searchQuery = it })
        Spacer(modifier = Modifier.height(16.dp))
        CategorySection()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Popular Recipes",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
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
            RecipeList(navController, meals, searchQuery, modifier )
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
                    painter = painterResource(id = R.drawable.ic_favorite), // Replace with your favorite icon resource
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { /* Handle favorite click */ }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate("profile") }
                )
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
fun CategorySection() {
    val categoryIcons = mapOf(
        "Popular" to R.drawable.ic_popular,
        "Beef" to R.drawable.ic_pizza,
        "Chicken" to R.drawable.ic_coffee,
        "Dessert" to R.drawable.ic_local,
        "Miscellaneous" to R.drawable.ic_desserts,
        "Pasta" to R.drawable.ic_local,
        "Pork" to R.drawable.ic_local,
        "Seafood" to R.drawable.ic_local,
        "Side" to R.drawable.ic_local,
        "Starter" to R.drawable.ic_local,
        "Vegan" to R.drawable.ic_local,
        "Vegetarian" to R.drawable.ic_local,
        "Breakfast" to R.drawable.ic_local,
        "Goat" to R.drawable.ic_local
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoryIcons.keys.toList()) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                /* Handle category click */
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = categoryIcons[category]!!),
                        contentDescription = category,
                        modifier = Modifier.size(30.dp),
                        tint = Color.White
                    )
                }
                Text(
                    text = category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RecipeList(navController: NavController, meals: List<Meal>,query:String,modifier: Modifier) {
    if (meals.isEmpty()) {
        Text(
            text = "There is no recipe called \"$query\"",
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
                RecipeItem(navController, currentMeal,modifier)
            }
        }
    }
}

@Composable
fun RecipeItem(navController: NavController, meal: Meal,modifier: Modifier) {

    Card(
        modifier = Modifier
            .clickable {
                MainActivity.staticMeal = meal
                navController.navigate("recipe") }
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column {
            val painter = rememberImagePainter(data = meal.strMealThumb)

            Image(
                painter = painter,
                contentDescription = null,
                modifier = modifier
                    .fillMaxWidth()
                    .height(140.dp), // Adjust the height as needed
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
                        text = "4.5 ★",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

