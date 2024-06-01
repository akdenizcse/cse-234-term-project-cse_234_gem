package com.example.recipefinder.api

import com.google.gson.Gson
import com.google.gson.JsonObject

fun jsonObjectToMeal(jsonObject: JsonObject): List<Meal> {
    val mealsJsonArray = jsonObject.getAsJsonArray("meals")
    val mealList = mutableListOf<Meal>()

    mealsJsonArray?.forEach { mealJsonElement ->
        val mealJson = mealJsonElement.asJsonObject
        val meal = Gson().fromJson(mealJson, Meal::class.java)
        mealList.add(meal)
    }

    return mealList
}
