package com.example.nutristrat.domain

import kotlin.math.sqrt

data class DailyNutritionSummary(
    val consumedCalories: Int,
    val targetCalories: Int,
    val consumedProtein: Int,
    val targetProtein: Int,
    val consumedCarbs: Int,
    val targetCarbs: Int,
    val consumedFat: Int,
    val targetFat: Int
)

data class CoachRecommendation(
    val title: String,
    val message: String,
    val suggestedFoods: List<String>,
    val status: NutritionStatus
)

enum class NutritionStatus {
    ON_TRACK,
    NEEDS_PROTEIN,
    NEEDS_ENERGY,
    OVER_LIMIT
}

data class FoodCandidate(
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

class NutritionRecommender {

    // Local food catalog with macro vectors (Protein, Carbs, Fat in grams)
    private val foodCatalog = listOf(
        // High Protein
        FoodCandidate("Grilled Tilapia", 220, protein = 40.0, carbs = 0.0, fat = 6.0),
        FoodCandidate("Grilled Chicken Breast", 250, protein = 45.0, carbs = 0.0, fat = 6.0),
        FoodCandidate("Fried Omena (1 cup)", 240, protein = 34.0, carbs = 2.0, fat = 10.0),
        FoodCandidate("Lean Beef Stew", 280, protein = 32.0, carbs = 6.0, fat = 14.0),
        FoodCandidate("Boiled Eggs (2)", 155, protein = 13.0, carbs = 1.0, fat = 11.0),
        FoodCandidate("Scrambled Eggs & Spinach", 210, protein = 16.0, carbs = 4.0, fat = 14.0),
        FoodCandidate("Plain Greek Yogurt (1 cup)", 140, protein = 18.0, carbs = 8.0, fat = 4.0),
        FoodCandidate("Tuna & Kachumbari Bowl", 230, protein = 30.0, carbs = 10.0, fat = 7.0),

        // Balanced Kenyan Staples
        FoodCandidate("Ndengu (Green Grams)", 230, protein = 16.0, carbs = 38.0, fat = 1.5),
        FoodCandidate("Githeri & Avocado", 480, protein = 18.0, carbs = 62.0, fat = 18.0),
        FoodCandidate("Rice & Beans (Mchele na Maharagwe)", 410, protein = 15.0, carbs = 78.0, fat = 4.0),
        FoodCandidate("Ugali, Sukuma & Beef Stew", 560, protein = 34.0, carbs = 68.0, fat = 16.0),
        FoodCandidate("Beef Pilau & Kachumbari", 520, protein = 24.0, carbs = 72.0, fat = 15.0),
        FoodCandidate("Mukimo & Beef Stew", 510, protein = 28.0, carbs = 64.0, fat = 15.0),
        FoodCandidate("Matoke & Lean Beef", 430, protein = 26.0, carbs = 58.0, fat = 10.0),
        FoodCandidate("Chapati (1) & Ndengu", 450, protein = 18.0, carbs = 68.0, fat = 11.0),
        FoodCandidate("Kamande (Lentils) & Rice", 390, protein = 17.0, carbs = 72.0, fat = 3.5),

        // Energy & Complex Carbs
        FoodCandidate("Ugali & Sukuma Wiki", 340, protein = 8.0, carbs = 70.0, fat = 3.0),
        FoodCandidate("Boiled Sweet Potatoes (Ngwaci)", 260, protein = 4.0, carbs = 60.0, fat = 0.5),
        FoodCandidate("Boiled Arrowroots (Nduma)", 240, protein = 3.5, carbs = 56.0, fat = 0.4),
        FoodCandidate("Oatmeal with Banana", 310, protein = 10.0, carbs = 58.0, fat = 5.0),

        // Healthy Fats & Dairy Snacks
        FoodCandidate("Plain Mala (1 cup)", 150, protein = 8.0, carbs = 12.0, fat = 8.0),
        FoodCandidate("Roasted Groundnuts (Handful)", 170, protein = 7.0, carbs = 6.0, fat = 14.0),
        FoodCandidate("Half Avocado with Lemon", 160, protein = 2.0, carbs = 8.5, fat = 15.0),

        // Light / Low-Calorie Options (Ideal when near or over limit)
        FoodCandidate("Steamed Managu / Terere", 75, protein = 5.0, carbs = 10.0, fat = 2.0),
        FoodCandidate("Kachumbari Salad", 60, protein = 2.0, carbs = 12.0, fat = 0.5),
        FoodCandidate("Watermelon Slices", 85, protein = 1.5, carbs = 21.0, fat = 0.4),
        FoodCandidate("Fresh Mango Slices", 100, protein = 1.4, carbs = 25.0, fat = 0.6)
    )

    fun generateRecommendation(summary: DailyNutritionSummary): CoachRecommendation {
        val remainingCalories = summary.targetCalories - summary.consumedCalories
        val remainingProtein = (summary.targetProtein - summary.consumedProtein).coerceAtLeast(0)
        val remainingCarbs = (summary.targetCarbs - summary.consumedCarbs).coerceAtLeast(0)
        val remainingFat = (summary.targetFat - summary.consumedFat).coerceAtLeast(0)

        // 1. Determine status & message
        val (status, title, message) = when {
            remainingCalories < -150 -> Triple(
                NutritionStatus.OVER_LIMIT,
                "Daily Target Exceeded",
                "You're ${-remainingCalories} kcal over your goal. Stick to very light, hydrating options."
            )
            remainingProtein >= 25 && remainingCalories >= 200 -> Triple(
                NutritionStatus.NEEDS_PROTEIN,
                "Boost Your Protein",
                "You still need ${remainingProtein}g of protein today to hit your target."
            )
            remainingCalories >= 500 && remainingCarbs >= 50 -> Triple(
                NutritionStatus.NEEDS_ENERGY,
                "Fuel Up with Balanced Energy",
                "You have $remainingCalories kcal left today. A balanced meal will keep your energy steady."
            )
            else -> Triple(
                NutritionStatus.ON_TRACK,
                "You're Right on Track!",
                "Great balance today. You have $remainingCalories kcal remaining if you need a light snack."
            )
        }

        // 2. Rank foods using Cosine Similarity against the user's remaining macro vector
        val suggestedFoods = if (status == NutritionStatus.OVER_LIMIT) {
            foodCatalog.sortedBy { it.calories }.take(3).map { it.name }
        } else {
            val needVector = doubleArrayOf(
                remainingProtein.toDouble(),
                remainingCarbs.toDouble(),
                remainingFat.toDouble()
            )

            foodCatalog
                .filter { it.calories <= (remainingCalories + 100).coerceAtLeast(150) }
                .map { food ->
                    val foodVector = doubleArrayOf(food.protein, food.carbs, food.fat)
                    val score = cosineSimilarity(needVector, foodVector)
                    food to score
                }
                .sortedByDescending { it.second }
                .take(4)
                .map { (food, score) ->
                    val matchPercent = (score * 100).toInt()
                    "${food.name} ($matchPercent%)"
                }
        }

        return CoachRecommendation(
            title = title,
            message = message,
            suggestedFoods = suggestedFoods,
            status = status
        )
    }

    internal fun cosineSimilarity(vecA: DoubleArray, vecB: DoubleArray): Double {
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in vecA.indices) {
            dotProduct += vecA[i] * vecB[i]
            normA += vecA[i] * vecA[i]
            normB += vecB[i] * vecB[i]
        }

        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator == 0.0) 0.0 else dotProduct / denominator
    }
}