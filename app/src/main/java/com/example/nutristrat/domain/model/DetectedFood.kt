package com.example.nutristrat.domain.model

data class DetectedFood(
 val id:Int,
 val name: String,
 val portion: String,
 val calories:Int,
 val protein:Double=0.0,
 val carbs:Double=0.0,
 val fat:Double=0.0,
)

data class MealAnalysisResult(
 val detectedFoods:List<DetectedFood>
){

 val totalCalories: Int
  get()=detectedFoods.sumOf{it.calories}
}