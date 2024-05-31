import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recipefinder.R
import com.example.recipefinder.ui.theme.MyTextField
import com.example.recipefinder.ui.theme.PasswordTextField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginPage(navController: NavController) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verifyPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    fun handleLogin() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate("home")
                } else {
                    Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun handleSignUp() {
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
                            Toast.makeText(context, "Profile update failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(context, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            Modifier.size(200.dp),
        )
        Text(
            text = if (isSignUp) "Sign Up" else "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AnimatedVisibility(
            visible = isSignUp,
            enter = slideInHorizontally(initialOffsetX = { it / 2 }),
            exit = slideOutHorizontally(targetOffsetX = { it / 2 })
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyTextField(
                    labelValue = stringResource(id = R.string.first_name),
                    painterResource(id = R.drawable.profile),
                    onTextChanged = { name = it }
                )
                MyTextField(
                    labelValue = stringResource(id = R.string.last_name),
                    painterResource = painterResource(id = R.drawable.profile),
                    onTextChanged = { surname = it }
                )
                MyTextField(
                    labelValue = stringResource(id = R.string.phone_number),
                    painterResource = painterResource(id = R.drawable.phone),
                    onTextChanged = { phoneNumber = it }
                )
            }
        }

        MyTextField(
            labelValue = stringResource(id = R.string.email),
            painterResource(id = R.drawable.email),
            onTextChanged = { email = it }
        )

        PasswordTextField(
            labelValue = stringResource(id = R.string.password),
            painterResource(id = R.drawable.baseline_lock_24),
            onTextSelected = { password = it }
        )

        AnimatedVisibility(
            visible = isSignUp,
            enter = slideInHorizontally(initialOffsetX = { it / 2 }),
            exit = slideOutHorizontally(targetOffsetX = { it / 2 })
        ) {
            PasswordTextField(
                labelValue = stringResource(id = R.string.verify_password),
                painterResource(id = R.drawable.baseline_lock_24),
                onTextSelected = { verifyPassword = it }
            )
        }

        Button(
            onClick = {
                if (isSignUp) {
                    handleSignUp()
                } else {
                    handleLogin()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = if (isSignUp) "Sign Up" else "Login")
        }

        Divider(
            color = Color.Black.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 28.dp)
        )

        Text(
            text = if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { isSignUp = !isSignUp }
        )
    }
}
