import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recipefinder.firebase.AuthHandler
import com.example.recipefinder.ui.theme.Navigation.UserViewModel

//import com.example.recipefinder.ui.theme.com.example.recipefinder.ui.theme.Navigation.UserViewModel


class UserViewModelFactory(private val authHandler: AuthHandler) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

