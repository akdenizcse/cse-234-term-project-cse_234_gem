package com.example.recipefinder.firebase


import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipefinder.ui.theme.Navigation.UserViewModel
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthHandler(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val favoritesCollection = db.collection("Favorites")


    fun addFavorite(mealId: String, userId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val favoriteRecipe = hashMapOf(
            "mealId" to mealId,
            "userId" to userId
        )
        favoritesCollection.add(favoriteRecipe)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Failed to add favorite: ${e.message}")
            }
    }

    fun removeFavorite(mealId: String, userId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        favoritesCollection.whereEqualTo("mealId", mealId)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    favoritesCollection.document(document.id).delete()
                }
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Failed to remove favorite: ${e.message}")
            }
    }


    fun getCurrentUser(callback: (UserProfile?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val docRef = db.collection("users").document(userId)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val userProfile = document.toObject(UserProfile::class.java)
                    callback(userProfile)
                } else {
                    callback(null)
                }
            }.addOnFailureListener {
                callback(null)
            }
        } else {
            callback(null)
        }
    }


data class UserProfile(
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

    fun handleLogin(
        email: String,
        password: String,
        navController: NavController
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate("home")
                } else {
                    Toast.makeText(
                        context,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun handleSignUp(
        email: String,
        password: String,
        verifyPassword: String,
        name: String,
        surname: String,
        phoneNumber: String,
        navController: NavController
    ) {
        if (password != verifyPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userProfile = hashMapOf(
                        "ID" to user?.uid,
                        "name" to name,
                        "surname" to surname,
                        "email" to email,
                        "phone" to phoneNumber,
                        "password" to password // Storing plain text passwords is not recommended.
                    )

                    firestore.collection("User").document(user?.uid ?: "")
                        .set(userProfile)
                        .addOnSuccessListener {
                           // UserViewModel.updateUserProfile("$name $surname", phoneNumber, email)
                            navController.navigate("home")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Profile update failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        context,
                        "Sign-up failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}

