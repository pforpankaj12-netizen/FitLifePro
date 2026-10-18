@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
 
package com.fitlifepro
 
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
 
private data class FoodEntry(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)
 
private data class WeightEntry(
    val date: String,
    val kg: Float
)
 
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FitLifeApp(this) }
    }
}
 
@Composable
private fun FitLifeApp(context: Context) {
    val prefs = remember {
        context.getSharedPreferences("fitlife_data", Context.MODE_PRIVATE)
    }
    var page by remember { mutableStateOf("Home") }
    var target by remember { mutableStateOf(prefs.getInt("target", 1900).toString()) }
    var water by remember { mutableStateOf(prefs.getInt("water", 0)) }
    var exercise by remember { mutableStateOf(prefs.getInt("exercise", 0)) }
    var weight by remember {
        mutableStateOf(prefs.getString("weight", "79.0") ?: "79.0")
    }
 
    val foods = remember {
        mutableStateListOf<FoodEntry>().apply {
            try {
                val json = JSONArray(prefs.getString("foods", "[]"))
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    add(FoodEntry(item.optString("name"), item.optInt("calories"),
                        item.optInt("protein"), item.optInt("carbs"), item.optInt("fat")))
                }
            } catch (_: Exception) {}
        }
    }
    val weights = remember {
        mutableStateListOf<WeightEntry>().apply {
            try {
                val json = JSONArray(prefs.getString("weights", "[]"))
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    add(WeightEntry(item.optString("date"), item.optDouble("kg").toFloat()))
                }
            } catch (_: Exception) {}
        }
    }
 
    fun saveFoods() {
        val json = JSONArray()
        foods.forEach { food ->
            json.put(JSONObject().apply {
                put("name", food.name)
                put("calories", food.calories)
                put("protein", food.protein)
                put("carbs", food.carbs)
                put("fat", food.fat)
            })
        }
        prefs.edit().putString("foods", json.toString()).apply()
    }
    fun saveWeights() {
        val json = JSONArray()
        weights.forEach { entry ->
            json.put(JSONObject().apply {
                put("date", entry.date)
                put("kg", entry.kg.toDouble())
            })
        }
        prefs.edit().putString("weights", json.toString()).apply()
    }
    fun saveNumber(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
 
    val totalCalories = foods.sumOf { it.calories }
    val totalProtein = foods.sumOf { it.protein }
    val totalCarbs = foods.sumOf { it.carbs }
    val totalFat = foods.sumOf { it.fat }
    val calorieTarget = target.toIntOrNull()?.coerceAtLeast(1) ?: 1900
    val currentWeight = weight.toFloatOrNull() ?: 79f
 
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF176B4D),
        secondary = Color(0xFF4F7D68),
        background = Color(0xFFF4F7F4),
        surface = Color.White
    )) {
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Column {
                        Text("FitLife Pro", style = MaterialTheme.typography.titleLarge)
                        Text(SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.labelSmall)
                    }
                })
            },
            bottomBar = {
                val destinations = listOf(
                    Triple("Home", Icons.Default.Home, "Home"),
                    Triple("Food", Icons.Default.Restaurant, "Food"),
                    Triple("Activity", Icons.Default.FitnessCenter, "Activity"),
                    Triple("Progress", Icons.Default.ShowChart, "Progress")
                )
                NavigationBar {
                    destinations.forEach { (name, icon, label) ->
                        NavigationBarItem(
                            selected = page == name,
                            onClick = { page = name },
                            icon = { androidx.compose.material3.Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = androidx.compose.ui.Modifier.fillMaxSize()
                    .padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    "Home" -> {
                        Text("Your daily dashboard", style = MaterialTheme.typography.headlineSmall)
                        Text("Small healthy choices add up. Keep going!")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard("Calories eaten", "$totalCalories kcal", Modifier.weight(1f))
                            StatCard("Daily target", "$calorieTarget kcal", Modifier.weight(1f))
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Daily calorie goal")
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (totalCalories.toFloat() / calorieTarget).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("${(calorieTarget - totalCalories).coerceAtLeast(0)} kcal remaining")
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard("Water", "$water ml", Modifier.weight(1f))
                            StatCard("Exercise", "$exercise min", Modifier.weight(1f))
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Today's nutrition", style = MaterialTheme.typography.titleMedium)
                                Text("Protein: $totalProtein g")
                                Text("Carbohydrates: $totalCarbs g")
                                Text("Fat: $totalFat g")
                            }
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Current weight", style = MaterialTheme.typography.titleMedium)
                                Text("$currentWeight kg", style = MaterialTheme.typography.headlineMedium)
                                Text("Weight records: ${weights.size}")
                            }
                        }
                        Button(onClick = { page = "Food" }, modifier = Modifier.fillMaxWidth()) {
                            Text("Log food")
                        }
                    }
                    "Food" -> {
                        var foodName by remember { mutableStateOf("") }
                        var caloriesText by remember { mutableStateOf("") }
                        var proteinText by remember { mutableStateOf("") }
                        var carbsText by remember { mutableStateOf("") }
                        var fatText by remember { mutableStateOf("") }
                        Text("Food diary", style = MaterialTheme.typography.headlineSmall)
                        Text("Record meals and keep track of your daily nutrition.")
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Add a food item", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(foodName, { foodName = it }, label = { Text("Food name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(caloriesText, { caloriesText = it }, label = { Text("Calories (kcal)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(proteinText, { proteinText = it }, label = { Text("Protein (g)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(carbsText, { carbsText = it }, label = { Text("Carbohydrates (g)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(fatText, { fatText = it }, label = { Text("Fat (g)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Button(onClick = {
                                    val c = caloriesText.toIntOrNull()
                                    val p = proteinText.toIntOrNull() ?: 0
                                    val cb = carbsText.toIntOrNull() ?: 0
                                    val f = fatText.toIntOrNull() ?: 0
                                    if (foodName.isNotBlank() && c != null && c >= 0 && p >= 0 && cb >= 0 && f >= 0) {
                                        foods.add(FoodEntry(foodName.trim(), c, p, cb, f))
                                        saveFoods()
                                        foodName = ""; caloriesText = ""; proteinText = ""; carbsText = ""; fatText = ""
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Text("Add to diary") }
                            }
                        }
                        Text("Today's food (${foods.size})", style = MaterialTheme.typography.titleLarge)
                        Text("Total: $totalCalories kcal")
                        if (foods.isEmpty()) Text("No food logged yet. Add your first meal above.")
                        foods.toList().forEachIndexed { index, food ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(food.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${food.calories} kcal")
                                    Text("Protein ${food.protein} g · Carbs ${food.carbs} g · Fat ${food.fat} g")
                                    Button(onClick = { foods.removeAt(index); saveFoods() }) { Text("Remove") }
                                }
                            }
                        }
                    }
                    "Activity" -> {
                        var minutesText by remember { mutableStateOf("") }
                        var targetText by remember { mutableStateOf(target) }
                        Text("Activity & habits", style = MaterialTheme.typography.headlineSmall)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Water tracker", style = MaterialTheme.typography.titleMedium)
                                Text("Today's recorded water: $water ml")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { water += 250; saveNumber("water", water) }) { Text("+250 ml") }
                                    Button(onClick = { water = (water - 250).coerceAtLeast(0); saveNumber("water", water) }) { Text("-250 ml") }
                                }
                                Button(onClick = { water = 0; saveNumber("water", water) }) { Text("Reset water") }
                            }
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Exercise tracker", style = MaterialTheme.typography.titleMedium)
                                Text("Recorded exercise: $exercise minutes")
                                OutlinedTextField(minutesText, { minutesText = it }, label = { Text("Exercise minutes") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Button(onClick = {
                                    val minutes = minutesText.toIntOrNull()
                                    if (minutes != null && minutes > 0) {
                                        exercise += minutes; saveNumber("exercise", exercise); minutesText = ""
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Text("Add exercise") }
                                Button(onClick = { exercise = 0; saveNumber("exercise", exercise) }) { Text("Reset exercise") }
                            }
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Daily calorie target", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(targetText, { targetText = it }, label = { Text("Calories per day") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Button(onClick = {
                                    val value = targetText.toIntOrNull()
                                    if (value != null && value > 0) { target = value.toString(); saveNumber("target", value) }
                                }, modifier = Modifier.fillMaxWidth()) { Text("Save calorie target") }
                            }
                        }
                    }
                    "Progress" -> {
                        var weightText by remember { mutableStateOf(weight) }
                        Text("Your progress", style = MaterialTheme.typography.headlineSmall)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Weight tracker", style = MaterialTheme.typography.titleMedium)
                                Text("Current weight: $currentWeight kg", style = MaterialTheme.typography.headlineSmall)
                                OutlinedTextField(weightText, { weightText = it }, label = { Text("Weight in kg") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Button(onClick = {
                                    val value = weightText.toFloatOrNull()
                                    if (value != null && value in 20f..500f) {
                                        weight = value.toString()
                                        prefs.edit().putString("weight", weight).apply()
                                        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                                        weights.add(WeightEntry(date, value))
                                        saveWeights()
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Text("Save weight record") }
                            }
                        }
                        Text("Weight history", style = MaterialTheme.typography.titleLarge)
                        if (weights.isEmpty()) Text("No weight records yet. Save your first entry above.")
                        weights.toList().asReversed().forEachIndexed { index, entry ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${entry.kg} kg", style = MaterialTheme.typography.titleMedium)
                                    Text(entry.date)
                                    Button(onClick = {
                                        val actualIndex = weights.lastIndex - index
                                        if (actualIndex in weights.indices) { weights.removeAt(actualIndex); saveWeights() }
                                    }) { Text("Remove record") }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Tip: weigh yourself under similar conditions each time. Look at the trend over several weeks rather than one reading.")
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
 
@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F2EA))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    
