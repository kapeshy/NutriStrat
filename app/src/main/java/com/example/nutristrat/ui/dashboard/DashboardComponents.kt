package com.example.nutristrat.ui.dashboard


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutristrat.domain.CoachRecommendation
import com.example.nutristrat.domain.DailyNutritionSummary
import com.example.nutristrat.domain.NutritionStatus

@Composable
fun CalorieProgressRing(
    consumed: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val progress = (consumed.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val remaining = target - consumed
    val ringColor = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(180.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$consumed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/ $target kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (remaining >= 0) "$remaining left" else "${-remaining} over",
                style = MaterialTheme.typography.labelMedium,
                color = ringColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MacroProgressSection(
    summary: DailyNutritionSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Macronutrients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            MacroBarRow("Protein", summary.consumedProtein, summary.targetProtein, Color(0xFF2E7D32))
            MacroBarRow("Carbs", summary.consumedCarbs, summary.targetCarbs, Color(0xFF1565C0))
            MacroBarRow("Fat", summary.consumedFat, summary.targetFat, Color(0xFFEF6C00))
        }
    }
}

@Composable
private fun MacroBarRow(
    label: String,
    consumed: Int,
    target: Int,
    color: Color
) {
    val progress = (consumed.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = "${consumed}g / ${target}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun NutritionCoachCard(
    recommendation: CoachRecommendation,
    modifier: Modifier = Modifier
) {
    val (containerColor, icon) = when (recommendation.status) {
        NutritionStatus.ON_TRACK -> MaterialTheme.colorScheme.primaryContainer to Icons.Default.CheckCircle
        NutritionStatus.NEEDS_PROTEIN -> MaterialTheme.colorScheme.secondaryContainer to Icons.Default.Star
        NutritionStatus.NEEDS_ENERGY -> MaterialTheme.colorScheme.tertiaryContainer to Icons.Default.Info
        NutritionStatus.OVER_LIMIT -> MaterialTheme.colorScheme.errorContainer to Icons.Default.Warning
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null)
                Text(
                    text = "AI Coach: ${recommendation.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = recommendation.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Suggested Options:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recommendation.suggestedFoods) { food ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(food) }
                    )
                }
            }
        }
    }
}

@Composable
fun MealSummaryRow(
    meal: DashboardMealItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${meal.time} • P:${meal.protein}g  C:${meal.carbs}g  F:${meal.fat}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${meal.calories} kcal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}