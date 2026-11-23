
package com.example.nutristrat.data.local.database

import com.example.nutristrat.data.local.entity.MealEntity

object MealCatalog {

    val meals = listOf(
        MealEntity(
            name = "Grilled Chicken",
            category = "Lunch",
            calories = 350,
            protein = 40.0,
            carbohydrates = 15.0,
            fats = 12.0,
            description = "Grilled chicken served with vegetables."
        ),
        MealEntity(
            name = "Vegetable Salad",
            category = "Lunch",
            calories = 180,
            protein = 6.0,
            carbohydrates = 20.0,
            fats = 8.0,
            description = "Fresh mixed vegetables with a light dressing."
        ),
        MealEntity(
            name = "Oatmeal",
            category = "Breakfast",
            calories = 250,
            protein = 9.0,
            carbohydrates = 42.0,
            fats = 5.0,
            description = "A nutritious bowl of oats."
        ),
        MealEntity(
            name = "Boiled Eggs",
            category = "Breakfast",
            calories = 155,
            protein = 13.0,
            carbohydrates = 1.0,
            fats = 11.0,
            description = "Two boiled eggs."
        ),
        MealEntity(
            name = "Ugali and Sukuma Wiki",
            category = "Lunch",
            calories = 320,
            protein = 8.0,
            carbohydrates = 55.0,
            fats = 7.0,
            description = "A Kenyan meal of ugali and sautéed sukuma wiki."
        ),
        MealEntity(
            name = "Beans and Rice",
            category = "Lunch",
            calories = 400,
            protein = 16.0,
            carbohydrates = 65.0,
            fats = 8.0,
            description = "Rice served with cooked beans."
        ),
        MealEntity(
            name = "Fruit Bowl",
            category = "Snack",
            calories = 150,
            protein = 2.0,
            carbohydrates = 35.0,
            fats = 1.0,
            description = "A bowl of assorted fresh fruits."
        ),
        MealEntity(
            name = "Avocado Toast",
            category = "Breakfast",
            calories = 280,
            protein = 8.0,
            carbohydrates = 30.0,
            fats = 15.0,
            description = "Whole-grain toast topped with avocado."
        )
    )
}
