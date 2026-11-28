package com.example.nutristrat

import android.os.Bundle
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.nutristrat.data.local.database.AppDatabase
import com.example.nutristrat.data.local.entity.UserEntity
import com.example.nutristrat.domain.DailyNutritionSummary
import com.example.nutristrat.domain.model.MealAnalysisResult
import com.example.nutristrat.ui.dashboard.DashboardMealItem
import com.example.nutristrat.ui.dashboard.DashboardViewModel
import com.example.nutristrat.ui.dashboard.NutritionSummaryScreen
import com.example.nutristrat.ui.dashboard.RecommendationsScreen
import com.example.nutristrat.ui.history.LoggedMealItem
import com.example.nutristrat.ui.history.MealHistoryScreen
import com.example.nutristrat.ui.history.sampleHistoryLogs
import com.example.nutristrat.ui.scanner.FoodAnalysisScreen
import com.example.nutristrat.ui.scanner.ScannerScreen
import com.example.nutristrat.ui.scanner.sampleDecodedFoods

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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppDatabase.getDatabase(applicationContext)

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

    val dashboardViewModel = remember { DashboardViewModel() }
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    var historyLogs by remember { mutableStateOf(sampleHistoryLogs()) }
    var lastAnalysisResult by remember { mutableStateOf(MealAnalysisResult(sampleDecodedFoods())) }

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
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate("welcome") {
                            popUpTo("splash") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

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
                val context = LocalContext.current
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
                                    val userName = response.user?.name ?: email.substringBefore("@")
                                    val userEmail = response.user?.email ?: email

                                    dashboardViewModel.setCurrentUser(userName, userEmail)

                                    try {
                                        AppDatabase.getDatabase(context).userDao().insertUser(
                                            UserEntity(name = userName, email = userEmail)
                                        )
                                    } catch (_: Exception) {
                                    }

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
                DashboardScreen(
                    userName = dashboardState.userName,
                    summary = dashboardState.summary,
                    todayMeals = dashboardState.todayMeals,
                    onScanMealClick = { navController.navigate("scanner") },
                    onProfileClick = { navController.navigate("profile") },
                    onSeeAllMealsClick = { navController.navigate("history") },
                    onNutritionClick = { navController.navigate("nutrition") }
                )
            }

            composable("scanner") {
                ScannerScreen(
                    onPhotoCaptured = {
                        lastAnalysisResult = MealAnalysisResult(sampleDecodedFoods())
                        navController.navigate("analysis")
                    },
                    onGalleryImageSelected = {
                        lastAnalysisResult = MealAnalysisResult(sampleDecodedFoods())
                        navController.navigate("analysis")
                    },
                    onSimulateScanClick = {
                        lastAnalysisResult = MealAnalysisResult(sampleDecodedFoods())
                        navController.navigate("analysis")
                    }
                )
            }

            composable("analysis") {
                FoodAnalysisScreen(
                    initialFoods = lastAnalysisResult.detectedFoods,
                    onBackClick = { navController.popBackStack() },
                    onSaveMealClick = { result ->
                        val totalCalories = result.totalCalories
                        val totalProtein = result.detectedFoods.sumOf { it.protein }.toInt()
                        val totalCarbs = result.detectedFoods.sumOf { it.carbs }.toInt()
                        val totalFat = result.detectedFoods.sumOf { it.fat }.toInt()
                        val mealName = result.detectedFoods.firstOrNull()?.name ?: "Scanned Meal"

                        val newDashboardMeal = DashboardMealItem(
                            name = mealName,
                            time = "Just now",
                            calories = totalCalories,
                            protein = totalProtein,
                            carbs = totalCarbs,
                            fat = totalFat
                        )
                        dashboardViewModel.addSampleMeal(newDashboardMeal)

                        val newLoggedMeal = LoggedMealItem(
                            id = (historyLogs.maxOfOrNull { it.id } ?: 0) + 1,
                            mealType = "Lunch",
                            description = result.detectedFoods.joinToString(", ") { it.name },
                            calories = totalCalories
                        )
                        historyLogs = historyLogs + newLoggedMeal

                        navController.navigate("recommendations")
                    }
                )
            }

            composable("recommendations") {
                RecommendationsScreen(
                    viewModel = dashboardViewModel,
                    onNavigateBack = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("nutrition") {
                NutritionSummaryScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToScanner = { navController.navigate("scanner") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToRecommendations = { navController.navigate("recommendations") }
                )
            }

            composable("history") {
                MealHistoryScreen(
                    initialLogs = historyLogs
                )
            }

            composable("profile") {
                ProfileScreen(
                    userName = dashboardState.userName,
                    userEmail = dashboardState.userEmail,
                    summary = dashboardState.summary,
                    onLogoutClick = {
                        navController.navigate("welcome") {
                            popUpTo("dashboard") {
                                inclusive = true
                            }
                        }
                    }
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
                text = apiError.ifBlank { error },
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
                text = apiError.ifBlank { error },
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
fun DashboardScreen(
    userName: String = "Guest",
    summary: DailyNutritionSummary = DailyNutritionSummary(
        consumedCalories = 0,
        targetCalories = 2200,
        consumedProtein = 0,
        targetProtein = 120,
        consumedCarbs = 0,
        targetCarbs = 260,
        consumedFat = 0,
        targetFat = 70
    ),
    todayMeals: List<DashboardMealItem> = emptyList(),
    onScanMealClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSeeAllMealsClick: () -> Unit = {},
    onNutritionClick: () -> Unit = {}
) {
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
                    "Let's eat well, $userName.",
                    color = ForestGreen,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LimeGreen, CircleShape)
                    .clickable { onProfileClick() },
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
                    .clickable { onScanMealClick() }
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNutritionClick() }
        ) {
            NutritionCard("Calories", "${summary.consumedCalories}", "kcal", Modifier.weight(1f))
            NutritionCard("Protein", "${summary.consumedProtein}", "g", Modifier.weight(1f))
            NutritionCard("Carbs", "${summary.consumedCarbs}", "g", Modifier.weight(1f))
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
            Text(
                "See all",
                color = MutedGreen,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onSeeAllMealsClick() }
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftWhite, RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (todayMeals.isEmpty()) {
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
            } else {
                todayMeals.takeLast(3).reversed().forEach { meal ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(SoftGreen, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🥗", fontSize = 23.sp)
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                meal.name,
                                color = ForestGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )

                            Spacer(Modifier.height(3.dp))

                            Text(
                                "${meal.time} • P:${meal.protein}g  C:${meal.carbs}g  F:${meal.fat}g",
                                color = MutedGreen,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            "${meal.calories} kcal",
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
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
fun ProfileScreen(
    userName: String,
    userEmail: String,
    summary: DailyNutritionSummary,
    onLogoutClick: () -> Unit
) {
    val initial = userName.firstOrNull()?.uppercase() ?: "U"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreen,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(LimeGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(ForestGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmCream
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userEmail.ifBlank { "No email provided" },
            fontSize = 14.sp,
            color = MutedGreen
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftWhite, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Daily Nutrition Targets",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileTargetItem("Calories", "${summary.targetCalories} kcal")
                ProfileTargetItem("Protein", "${summary.targetProtein}g")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileTargetItem("Carbs", "${summary.targetCarbs}g")
                ProfileTargetItem("Fat", "${summary.targetFat}g")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB3261E)
            )
        ) {
            Text(
                text = "Log Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ProfileTargetItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(SoftGreen, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .width(130.dp)
    ) {
        Text(label, color = MutedGreen, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = ForestGreen,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
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