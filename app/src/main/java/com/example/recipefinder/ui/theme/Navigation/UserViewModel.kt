package com.example.recipefinder.ui.theme.Navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {
    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> = _fullName

    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> = _phoneNumber

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchUserData()
    }
    fun updateUserProfile(fullName: String, phoneNumber: String, email: String) {
        _fullName.value = fullName
        _phoneNumber.value = phoneNumber
        _email.value = email
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                val userDoc = db.collection("users").document(userId).get().await()
                _fullName.value = userDoc.getString("fullName")
                _phoneNumber.value = userDoc.getString("phoneNumber")
                _email.value = userDoc.getString("email")
            }
        }
    }
}
