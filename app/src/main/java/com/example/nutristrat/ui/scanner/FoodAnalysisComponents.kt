package com.example.nutristrat.ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutristrat.domain.model.DetectedFood


@Composable
fun DetectedFoodRow(
    food: DetectedFood,
    badgeColor:Color=Color(0xFFF3C69F)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Colored circular food icon badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(12.dp))

            //Food Name & Portion stacked vertically
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1B3B2B)
                )
                Text(
                    text = food.portion,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
// Calorie value aligned on the right
            Text(
                text = " ${food.calories} kcal",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF2E6F40)
            )
        }
    }
}

@Composable
fun EstimatedEnergyCard(totalCalories:Int){
    Card(
        modifier=Modifier.fillMaxWidth(),
        shape=RoundedCornerShape(16.dp),
        colors=CardDefaults.cardColors(containerColor=Color(0xFF1B3B2B))
    ){
        Column(modifier=Modifier.padding(20.dp)) {
            Text(
                text = "ESTIMATED ENERGY",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA5D6A7),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$totalCalories kcal",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "estimate . adjust portions for better accuracy",
                fontSize = 12.sp,
                color = Color (0xFFB0BEC5)
            )
        }
        }
    }
