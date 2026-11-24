package com.example.nutristrat.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LoggedMealItem(
    val id: Int,
    val mealType: String, // e.g Breakfast,Lunch
    val description: String,
    val calories: Int
)

@Composable
fun MealHistoryScreen(
    initialLogs: List<LoggedMealItem> = sampleHistoryLogs()
) {
    var mealLogs by remember { mutableStateOf(initialLogs) }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Breakfast", "Lunch", "Dinner")

    // Functional filter pipeline: shows all meals or filters by selected category
    val filteredLogs = if (selectedCategory == "All") {
        mealLogs
    } else {
        mealLogs.filter { it.mealType.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5EE))
            .padding(20.dp)
    ) {
        Text(
            text = "Meal history",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B3B2B)
        )
        Text(
            text = "Total logged: ${filteredLogs.sumOf { it.calories }} kcal",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter Chips (All, Breakfast, Lunch, Dinner)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No meals logged in this category yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = log.mealType,
                                    tint = Color(0xFF2E6F40),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.mealType,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1B3B2B)
                                )
                                Text(
                                    text = log.description,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "${log.calories} kcal",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E6F40)
                            )
                            IconButton(
                                onClick = {
                                    mealLogs = mealLogs.filter { it.id != log.id }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete log",
                                    tint = Color(0xFFBDBDBD)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun sampleHistoryLogs() = listOf(
    LoggedMealItem(1, "Breakfast", "Oats, banana & yoghurt", 380),
    LoggedMealItem(2, "Lunch", "Chicken, rice & greens", 560),
    LoggedMealItem(3, "Dinner", "Grilled chicken, steamed rice & sukuma wiki", 505),
)
