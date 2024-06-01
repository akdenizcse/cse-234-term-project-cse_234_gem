package com.example.recipefinder.firebase

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthHandler(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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
