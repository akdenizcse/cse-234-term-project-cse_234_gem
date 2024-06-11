package com.example.recipefinder

import com.google.firebase.Timestamp

data class Comment(
    var id: String? = null,            // Unique identifier for the comment
    var userId: String? = null,        // ID of the user who posted the comment
    var userName: String? = null,      // Name of the user who posted the comment
    var userSurname: String? = null,   // Surname of the user who posted the comment
    var text: String? = null,          // The comment text
    var rating: Double = 0.0,          // The rating associated with the comment
    var timestamp: Timestamp = Timestamp.now()  // The time the comment was posted
)
