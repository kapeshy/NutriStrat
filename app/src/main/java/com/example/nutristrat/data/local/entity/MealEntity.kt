
package com.example.nutristrat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val calories: Int,
    val protein: Double,
    val carbohydrates: Double,
    val fats: Double,
    val description: String,
    val imageUrl: String = ""
)
