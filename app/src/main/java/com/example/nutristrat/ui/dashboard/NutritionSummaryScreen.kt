package com.example.nutristrat.ui.dashboard


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionSummaryScreen(
    viewModel: DashboardViewModel = remember { DashboardViewModel() },
    onNavigateToScanner: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToRecommendations: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello, ${uiState.userName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Today's Nutrition Overview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToScanner,
                icon = { Icon(Icons.Default.Add, contentDescription = "Scan Meal") },
                text = { Text("Scan Meal") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Calorie Ring
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CalorieProgressRing(
                        consumed = uiState.summary.consumedCalories,
                        target = uiState.summary.targetCalories
                    )
                }
            }

            // 2. Macronutrient Progress Bars
            item {
                MacroProgressSection(summary = uiState.summary)
            }

            // 3. Personalized AI Nutrition Coach
            item {
                NutritionCoachCard(recommendation = uiState.recommendation)
            }

            // 4. Today's Meals Header + Quick Demo Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Meals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            // Demo button to test live ring & AI coach updates
                            viewModel.addSampleMeal(
                                DashboardMealItem(
                                    name = "Grilled Tilapia & Ugali",
                                    time = "07:30 PM",
                                    calories = 580,
                                    protein = 42,
                                    carbs = 65,
                                    fat = 14
                                )
                            )
                        }
                    ) {
                        Text("+ Test Log")
                    }
                }
            }

            // 5. Meal List
            items(uiState.todayMeals) { meal ->
                MealSummaryRow(meal = meal)
            }

            // Bottom spacer so FAB doesn't cover the last meal card
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    MaterialTheme {
        NutritionSummaryScreen()
    }
}