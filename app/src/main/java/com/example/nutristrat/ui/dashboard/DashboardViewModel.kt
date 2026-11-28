package com.example.nutristrat.ui.dashboard


import androidx.lifecycle.ViewModel
import com.example.nutristrat.domain.CoachRecommendation
import com.example.nutristrat.domain.DailyNutritionSummary
import com.example.nutristrat.domain.NutritionRecommender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DashboardMealItem(
    val name: String,
    val time: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class DashboardUiState(
    val userName: String = "Guest",
    val userEmail: String = "",
    val summary: DailyNutritionSummary,
    val recommendation: CoachRecommendation,
    val todayMeals: List<DashboardMealItem>
)

class DashboardViewModel(
    private val recommender: NutritionRecommender = NutritionRecommender()
) : ViewModel() {

    private val defaultTargets = Triple(2200, 120, 260) // Calories, Protein(g), Carbs(g)
    private val targetFat = 70

    private val initialMeals = listOf(
        DashboardMealItem("Mandazi (2) & Chai", "08:15 AM", calories = 420, protein = 8, carbs = 62, fat = 16),
        DashboardMealItem("Githeri & Avocado", "01:10 PM", calories = 650, protein = 24, carbs = 88, fat = 22)
    )

    private val _uiState = MutableStateFlow(buildState(initialMeals))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun setCurrentUser(name: String, email: String) {
        _uiState.update { current ->
            current.copy(
                userName = name.ifBlank { "User" },
                userEmail = email
            )
        }
    }

    private fun buildState(meals: List<DashboardMealItem>): DashboardUiState {
        val summary = DailyNutritionSummary(
            consumedCalories = meals.sumOf { it.calories },
            targetCalories = defaultTargets.first,
            consumedProtein = meals.sumOf { it.protein },
            targetProtein = defaultTargets.second,
            consumedCarbs = meals.sumOf { it.carbs },
            targetCarbs = defaultTargets.third,
            consumedFat = meals.sumOf { it.fat },
            targetFat = targetFat
        )
        return DashboardUiState(
            summary = summary,
            recommendation = recommender.generateRecommendation(summary),
            todayMeals = meals
        )
    }

    /**
     * Handy for demoing live updates to the Calorie Ring and AI Coach
     * before Member 1's Room database is merged.
     */
    fun addSampleMeal(meal: DashboardMealItem) {
        _uiState.update { current ->
            val updatedMeals = current.todayMeals + meal
            val updatedSummary = current.summary.copy(
                consumedCalories = updatedMeals.sumOf { it.calories },
                consumedProtein = updatedMeals.sumOf { it.protein },
                consumedCarbs = updatedMeals.sumOf { it.carbs },
                consumedFat = updatedMeals.sumOf { it.fat }
            )
            current.copy(
                summary = updatedSummary,
                recommendation = recommender.generateRecommendation(updatedSummary),
                todayMeals = updatedMeals
            )
        }
    }
}