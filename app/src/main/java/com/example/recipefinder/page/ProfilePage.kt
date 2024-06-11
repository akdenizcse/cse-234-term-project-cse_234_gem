package com.example.recipefinder.page

import com.example.recipefinder.ui.theme.Navigation.UserViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recipefinder.R
//import com.example.recipefinder.ui.theme.com.example.recipefinder.ui.theme.Navigation.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfilePage(navController: NavController, userViewModel: UserViewModel) {
    val phoneNumber by userViewModel.phoneNumber.observeAsState("")
    val email by userViewModel.email.observeAsState("")
    val fullName by userViewModel.fullName.observeAsState("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(fullName)
        Spacer(modifier = Modifier.height(16.dp))
        UserInfo(phoneNumber = phoneNumber, email = email)
        Spacer(modifier = Modifier.weight(1f))
        ChangePasswordButton()
        Spacer(modifier = Modifier.height(16.dp))
        LogOutButton(navController)
    }
}

@Composable
fun ProfileHeader(fullName: String) {
    Image(
        painter = painterResource(id = R.drawable.cookerlogo),
        contentDescription = null,
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
    )
    Text(
        text = fullName,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun UserInfo(phoneNumber: String, email: String) {
    Text(
        text = "Phone Number : $phoneNumber",
        fontSize = 16.sp,
        modifier = Modifier.padding(8.dp)
    )
    Text(
        text = "Email : $email",
        fontSize = 16.sp,
        color = Color.Black,
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun ChangePasswordButton() {
    Button(
        onClick = { /* Şifre değiştirme işlemi burada yapılacak */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
    ) {
        Text(text = "Change Password")
    }
}

@Composable
fun LogOutButton(navController: NavController) {
    Button(
        onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(50.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = "Log Out")
    }
}
