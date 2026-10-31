package com.example.nutristrat

import android.os.Bundle
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nutristrat.ui.theme.NutriStratTheme
import com.example.nutristrat.data.remote.RegisterRequest
import com.example.nutristrat.data.remote.LoginRequest
import com.example.nutristrat.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

private val ForestGreen = Color(0xFF234D3C)
private val WarmCream = Color(0xFFFAF8F0)
private val LimeGreen = Color(0xFFD5E8A5)
private val MutedGreen = Color(0xFF718477)
private val SoftWhite = Color(0xFFFFFFFF)
private val SoftGreen = Color(0xFFF0F3E9)

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination("dashboard", "Home", Icons.Default.Home),
    BottomDestination("nutrition", "Nutrition", Icons.Default.BarChart),
    BottomDestination("history", "History", Icons.Default.History),
    BottomDestination("profile", "Profile", Icons.Default.Person)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NutriStratTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WarmCream
                ) {
                    AppNavigation(navController)
                }
            }
        }
    }
}

private fun getApiError(exception: Exception): String {
    return if (exception is HttpException) {
        try {
            val errorBody = exception.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val jsonObj = JSONObject(errorBody)
                jsonObj.optString("message", "Something went wrong. Please try again.")
            } else {
                "Server returned status code: ${exception.code()}"
            }
        } catch (e: Exception) {
            "Error parsing response: ${e.localizedMessage}"
        }
    } else {
        // Raw network/connection exception details
        "Network error: ${exception.localizedMessage ?: "Unable to connect to the server."}"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val mainRoutes = listOf("dashboard", "nutrition", "history", "profile")
    val showBottomBar = currentRoute in mainRoutes

    val scope = rememberCoroutineScope()

    var registerLoading by remember { mutableStateOf(false) }
    var loginLoading by remember { mutableStateOf(false) }

    var registerApiError by remember { mutableStateOf("") }
    var loginApiError by remember { mutableStateOf("") }

    Scaffold(
        containerColor = WarmCream,
        bottomBar = {
            if (showBottomBar) {
                NutriBottomBar(
                    currentRoute = currentRoute ?: "dashboard",
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo("dashboard") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("welcome") {
                WelcomeScreen(
                    onGetStarted = {
                        registerApiError = ""
                        navController.navigate("register")
                    },
                    onLogin = {
                        loginApiError = ""
                        navController.navigate("login")
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegister = { name, email, password ->

                        registerApiError = ""

                        scope.launch {
                            registerLoading = true

                            try {
                                val response = RetrofitClient.api.register(
                                    RegisterRequest(
                                        name = name,
                                        email = email,
                                        password = password
                                    )
                                )

                                if (response.success) {
                                    navController.navigate("login") {
                                        popUpTo("welcome")
                                    }
                                } else {
                                    registerApiError = response.message
                                }

                            } catch (e: Exception) {
                                registerApiError = getApiError(e)
                            } finally {
                                registerLoading = false
                            }
                        }
                    },
                    onLogin = {
                        registerApiError = ""
                        navController.navigate("login")
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    apiError = registerApiError,
                    isLoading = registerLoading
                )
            }

            composable("login") {
                LoginScreen(
                    onLogin = { email, password ->

                        loginApiError = ""

                        scope.launch {
                            loginLoading = true

                            try {
                                val response = RetrofitClient.api.login(
                                    LoginRequest(
                                        email = email,
                                        password = password
                                    )
                                )

                                if (response.success) {
                                    navController.navigate("dashboard") {
                                        popUpTo("welcome") {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    loginApiError = response.message
                                }

                            } catch (e: Exception) {
                                loginApiError = getApiError(e)
                            } finally {
                                loginLoading = false
                            }
                        }
                    },
                    onRegister = {
                        loginApiError = ""
                        navController.navigate("register")
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    apiError = loginApiError,
                    isLoading = loginLoading
                )
            }

            composable("dashboard") {
                DashboardScreen()
            }

            composable("nutrition") {
                PlaceholderScreen(
                    title = "Nutrition",
                    description = "Your nutrition summary will appear here."
                )
            }

            composable("history") {
                PlaceholderScreen(
                    title = "Meal History",
                    description = "Your saved meals will appear here."
                )
            }

            composable("profile") {
                PlaceholderScreen(
                    title = "Your Profile",
                    description = "Your personal details and preferences will appear here."
                )
            }
        }
    }
}

@Composable
fun NutriBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = SoftWhite,
        contentColor = ForestGreen
    ) {
        bottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForestGreen,
                    selectedTextColor = ForestGreen,
                    indicatorColor = LimeGreen,
                    unselectedIconColor = MutedGreen,
                    unselectedTextColor = MutedGreen
                )
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "NutriStrat",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(220.dp)
                        .background(LimeGreen, RoundedCornerShape(120.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(140.dp)
                            .background(ForestGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "N",
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmCream
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Eat better. Feel better.",
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreen,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Discover what's on your plate, understand your nutrition, and build healthier habits one meal at a time.",
                fontSize = 16.sp,
                lineHeight = 25.sp,
                color = MutedGreen,
                textAlign = TextAlign.Center
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen
                )
            ) {
                Text("Get Started", fontSize = 17.sp)
            }

            TextButton(onClick = onLogin) {
                Text("Already have an account? Log in", color = ForestGreen)
            }

            Text(
                "YOUR JOURNEY TO MINDFUL EATING",
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = MutedGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    apiError: String = "",
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", color = ForestGreen)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Create your account",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Start building healthier habits with NutriStrat.",
            color = MutedGreen
        )

        Spacer(Modifier.height(24.dp))

        NutriField(name, { name = it; error = "" }, "Full name")

        Spacer(Modifier.height(14.dp))

        NutriField(
            email, { email = it; error = "" },
            "Email address", keyboardType = KeyboardType.Email
        )

        Spacer(Modifier.height(14.dp))

        NutriField(
            password, { password = it; error = "" },
            "Password", password = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(Modifier.height(14.dp))

        NutriField(
            confirmPassword, { confirmPassword = it; error = "" },
            "Confirm password", password = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        if (error.isNotBlank() || apiError.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (apiError.isNotBlank()) apiError else error,
                color = Color(0xFFB3261E),
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                when {
                    name.isBlank() || email.isBlank() ||
                            password.isBlank() || confirmPassword.isBlank() ->
                        error = "Please fill in all fields."

                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email.trim()).matches() ->
                        error = "Please enter a valid email address."

                    password.length < 8 ->
                        error = "Password must be at least 8 characters."

                    password != confirmPassword ->
                        error = "Your passwords do not match."

                    else -> onRegister(name.trim(), email.trim(), password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Text(
                if (isLoading) "Creating account..." else "Create Account",
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already registered?", color = MutedGreen)
            TextButton(onClick = onLogin) {
                Text("Log in", color = ForestGreen)
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
    apiError: String = "",
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", color = ForestGreen)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Welcome back",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(Modifier.height(8.dp))

        Text("Log in to continue your nutrition journey.", color = MutedGreen)

        Spacer(Modifier.height(28.dp))

        NutriField(
            email, { email = it; error = "" },
            "Email address", keyboardType = KeyboardType.Email
        )

        Spacer(Modifier.height(16.dp))

        NutriField(
            password, { password = it; error = "" },
            "Password", password = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        if (error.isNotBlank() || apiError.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (apiError.isNotBlank()) apiError else error,
                color = Color(0xFFB3261E),
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() ->
                        error = "Enter your email and password."

                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email.trim()).matches() ->
                        error = "Please enter a valid email address."

                    else -> onLogin(email.trim(), password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
            Text(
                if (isLoading) "Logging in..." else "Log In",
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("New to NutriStrat?", color = MutedGreen)
            TextButton(onClick = onRegister) {
                Text("Create account", color = ForestGreen)
            }
        }
    }
}

@Composable
fun NutriField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (password) {
            {
                TextButton(onClick = onTogglePassword) {
                    Text(
                        if (passwordVisible) "Hide" else "Show",
                        color = ForestGreen
                    )
                }
            }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ForestGreen,
            focusedLabelColor = ForestGreen,
            cursorColor = ForestGreen
        )
    )
}

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Good morning,", color = MutedGreen, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Let's eat well.",
                    color = ForestGreen,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LimeGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = ForestGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ForestGreen, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", color = LimeGreen, fontSize = 28.sp)
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "What's on your plate?",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Discover the nutrition behind your meals.",
                color = Color(0xFFE1E9E1),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LimeGreen, RoundedCornerShape(14.dp))
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Scan your meal",
                    color = ForestGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Today's nutrition",
            color = ForestGreen,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NutritionCard("Calories", "—", "kcal", Modifier.weight(1f))
            NutritionCard("Protein", "—", "grams", Modifier.weight(1f))
            NutritionCard("Water", "—", "glasses", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent meals",
                color = ForestGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text("See all", color = MutedGreen, fontSize = 13.sp)
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftWhite, RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(SoftGreen, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍳", fontSize = 23.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        "No meals recorded yet",
                        color = ForestGreen,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        "Your saved meals will appear here.",
                        color = MutedGreen,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Nutrition values will appear when meal tracking is implemented.",
            color = MutedGreen,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun NutritionCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SoftWhite, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 10.dp)
    ) {
        Text(title, color = MutedGreen, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            color = ForestGreen,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(3.dp))
        Text(unit, color = MutedGreen, fontSize = 10.sp)
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = ForestGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            description,
            color = MutedGreen,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}