package com.example.nutristrat.ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutristrat.domain.model.DetectedFood
import com.example.nutristrat.domain.model.MealAnalysisResult

@Composable
fun FoodAnalysisScreen(
    initialFoods: List<DetectedFood> = sampleDecodedFoods(),
    onBackClick: () -> Unit = {},
    onSaveMealClick: (MealAnalysisResult) -> Unit = {}
) {
    // 1. State that survives recomposition and updates UI automatically
    var detectedFoods by remember { mutableStateOf(initialFoods) }
    var isEditMode by remember { mutableStateOf(false) }

    // 2. Derived state: automatically recalculates totalCalories whenever detectedFoods changes
    val analysisResult = MealAnalysisResult(detectedFoods)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5EE))
            .padding(16.dp)
    ) {
        // Top Header ("< Your meal, decoded")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1B3B2B)
                )
            }
            Text(
                text = "Your meal, decoded",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B3B2B)
            )
        }

        Text(
            text = "Here's what we think is on your plate.",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
        )

        // Plate Preview Card with "3 foods found" pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFFEBE6D5), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Text(
                    text = "✓ ${detectedFoods.size} foods found",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B3B2B),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Header: "Detected foods" + interactive "Edit" toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detected foods",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1B3B2B)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isEditMode = !isEditMode }
            ) {
                Text(
                    text = if (isEditMode) "Done" else "Edit",
                    color = Color(0xFF2E6F40),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit foods",
                    tint = Color(0xFF2E6F40),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic List of Detected Foods (LazyColumn = Compose's RecyclerView)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(detectedFoods, key = { it.id }) { food ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetectedFoodRow(food = food)
                    }
                    if (isEditMode) {
                        IconButton(
                            onClick = {
                                detectedFoods = detectedFoods.filter { it.id != food.id }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove ${food.name}",
                                tint = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Estimated Energy Card (updates live!)
        EstimatedEnergyCard(totalCalories = analysisResult.totalCalories)

        Spacer(modifier = Modifier.height(12.dp))

        // Primary CTA Button: "Save meal & view insights ->"
        Button(
            onClick = { onSaveMealClick(analysisResult) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6F40))
        ) {
            Text(
                text = "Save meal & view insights  →",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

fun sampleDecodedFoods() = listOf(
    DetectedFood(1, "Grilled chicken", "~120 g", 245, protein = 31.0, carbs = 0.0, fat = 12.0),
    DetectedFood(2, "Steamed rice", "~1 cup", 205, protein = 4.3, carbs = 45.0, fat = 0.4),
    DetectedFood(3, "Sukuma wiki", "~½ cup", 55, protein = 2.5, carbs = 8.0, fat = 1.5)
)