
package com.fitlifepro

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private data class FoodEntry(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

private data class WeightEntry(val date: String, val kg: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FitLifeApp(this) }
    }
}

@Composable
fun FitLifeApp(context: Context) {
    val prefs = remember {
        context.getSharedPreferences("fitlife_data", Context.MODE_PRIVATE)
    }

    var page by remember { mutableStateOf("Home") }
    var target by remember { mutableIntStateOf(prefs.getInt("target", 1900)) }
    var water by remember { mutableIntStateOf(prefs.getInt("water", 0)) }
    var exercise by remember { mutableIntStateOf(prefs.getInt("exercise", 0)) }
    var weight by remember { mutableStateOf(prefs.getString("weight", "79.0") ?: "79.0") }

    val foods = remember {
        mutableStateListOf<FoodEntry>().also { list ->
            try {
                val json = JSONArray(prefs.getString("foods", "[]"))
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    list.add(
                        FoodEntry(
                            item.optString("name"),
                            item.optInt("calories"),
                            item.optInt("protein"),
                            item.optInt("carbs"),
                            item.optInt("fat")
                        )
                    )
                }
            } catch (_: Exception) {}
        }
    }

    val weights = remember {
        mutableStateListOf<WeightEntry>().also { list ->
            try {
                val json = JSONArray(prefs.getString("weights", "[]"))
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    list.add(
                        WeightEntry(
                            item.optString("date"),
                            item.optDouble("kg").toFloat()
                        )
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun saveFoods() {
        val json = JSONArray()
        foods.forEach {
            json.put(JSONObject().apply {
                put("name", it.name)
                put("calories", it.calories)
                put("protein", it.protein)
                put("carbs", it.carbs)
                put("fat", it.fat)
            })
        }
        prefs.edit().putString("foods", json.toString()).apply()
    }

    fun saveWeights() {
        val json = JSONArray()
        weights.forEach {
            json.put(JSONObject().apply {
                put("date", it.date)
                put("kg", it.kg.toDouble())
            })
        }
        prefs.edit().putString("weights", json.toString()).apply()
    }

    val calories = foods.sumOf { it.calories }
    val protein = foods.sumOf { it.protein }
    val carbs = foods.sumOf { it.carbs }
    val fat = foods.sumOf { it.fat }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF176B4D),
            secondary = androidx.compose.ui.graphics.Color(0xFF4F7D68),
            background = androidx.compose.ui.graphics.Color(0xFFF4F7F4)
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("FitLife Pro", fontWeight = FontWeight.Bold)
                            Text(
                                LocalDate.now().toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    listOf(
                        "Home", "Food", "Activity", "Progress"
                    ).forEach { name ->
                        val icon = when (name) {
                            "Home" -> Icons.Default.Home
                            "Food" -> Icons.Default.Restaurant
                            "Activity" -> Icons.Default.FitnessCenter
                            else -> Icons.Default.ShowChart
                        }
                        NavigationBarItem(
                            selected = page == name,
                            onClick = { page = name },
                            icon = { Icon(icon, contentDescription = name) },
                            label = { Text(name) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    "Home" -> {
                        Text("Your daily dashboard", style = MaterialTheme.typography.titleLarge)

                        Card {
                            Column(
                                Modifier.fillMaxWidth().padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Calories consumed")
                                Text(
                                    "$calories / $target kcal",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                LinearProgressIndicator(
                                    progress = {
                                        (calories.toFloat() / target.coerceAtLeast(1))
                                            .coerceIn(0f, 1f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("${(target - calories).coerceAtLeast(0)} kcal remaining")
                                Text("Daily target: $target kcal")
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SummaryCard("Water", "$water ml", Modifier.weight(1f))
                            SummaryCard("Workout", "$exercise min", Modifier.weight(1f))
                        }

                        Card {
                            Column(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Nutrition today", fontWeight = FontWeight.Bold)
                                Text("Protein: $protein g")
                                Text("Carbohydrates: $carbs g")
                                Text("Fat: $fat g")
                            }
                        }

                        Card {
                            Column(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Quick actions", fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { water += 250
                                        prefs.edit().putInt("water", water).apply()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("＋ Add 250 ml water") }
                                OutlinedButton(
                                    onClick = { exercise += 10
                                        prefs.edit().putInt("exercise", exercise).apply()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("＋ Log 10 minutes exercise") }
                                OutlinedButton(
                                    onClick = { page = "Food" },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("＋ Log food") }
                            }
                        }
                    }

                    "Food" -> {
                        var name by remember { mutableStateOf("") }
                        var kcal by remember { mutableStateOf("150") }
                        var p by remember { mutableStateOf("5") }
                        var c by remember { mutableStateOf("20") }
                        var f by remember { mutableStateOf("4") }
                        var targetText by remember(target) {
                            mutableStateOf(target.toString())
                        }

                        Text("Food diary", style = MaterialTheme.typography.titleLarge)
                        Text("$calories kcal consumed today")

                        OutlinedTextField(
                            name, { name = it },
                            label = { Text("Food name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            kcal, { kcal = it.filter(Char::isDigit) },
                            label = { Text("Calories (kcal)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                p, { p = it.filter(Char::isDigit) },
                                label = { Text("Protein (g)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                c, { c = it.filter(Char::isDigit) },
                                label = { Text("Carbs (g)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                f, { f = it.filter(Char::isDigit) },
                                label = { Text("Fat (g)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                val amount = kcal.toIntOrNull()
                                if (name.isNotBlank() && amount != null && amount > 0) {
                                    foods.add(
                                        FoodEntry(
                                            name.trim(), amount,
                                            p.toIntOrNull() ?: 0,
                                            c.toIntOrNull() ?: 0,
                                            f.toIntOrNull() ?: 0
                                        )
                                    )
                                    saveFoods()
                                    name = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save food entry") }

                        HorizontalDivider()
                        Text("Set calorie target", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            targetText, { targetText = it.filter(Char::isDigit) },
                            label = { Text("Daily target (kcal)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = {
                                targetText.toIntOrNull()
                                    ?.takeIf { it in 500..10000 }
                                    ?.let {
                                        target = it
                                        prefs.edit().putInt("target", it).apply()
                                    }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save target") }

                        HorizontalDivider()
                        Text("Today's food entries", fontWeight = FontWeight.Bold)
                        if (foods.isEmpty()) Text("No food entries yet.")
                        foods.forEachIndexed { index, item ->
                            Card {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.SemiBold)
                                        Text("${item.calories} kcal")
                                        Text(
                                            "P ${item.protein} g · C ${item.carbs} g · F ${item.fat} g",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = {
                                        foods.removeAt(index)
                                        saveFoods()
                                    }) {
                                        Icon(Icons.Default.Delete, "Delete entry")
                                    }
                                }
                            }
                        }
                    }

                    "Activity" -> {
                        var kind by remember { mutableStateOf("Walking") }
                        var minutes by remember { mutableStateOf("20") }

                        Text("Workout tracker", style = MaterialTheme.typography.titleLarge)
                        Text("Total exercise logged: $exercise minutes")

                        Text("Activity type")
                        listOf("Walking", "Running", "Cycling", "Strength training", "Yoga", "Other")
                            .forEach { option ->
                                Row {
                                    RadioButton(
                                        selected = kind == option,
                                        onClick = { kind = option }
                                    )
                                    Text(option, modifier = Modifier.padding(top = 12.dp))
                                }
                            }

                        OutlinedTextField(
                            minutes, { minutes = it.filter(Char::isDigit) },
                            label = { Text("Duration in minutes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                minutes.toIntOrNull()?.takeIf { it in 1..600 }?.let {
                                    exercise += it
                                    prefs.edit().putInt("exercise", exercise).apply()
                                    minutes = "20"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save workout") }

                        Text(
                            "Selected activity: $kind",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Workout minutes are saved. Calorie-burn estimates are not included yet."
                        )
                    }

                    "Progress" -> {
                        var draft by remember(weight) { mutableStateOf(weight) }

                        Text("Your progress", style = MaterialTheme.typography.titleLarge)
                        SummaryCard("Current weight", "$weight kg")
                        SummaryCard("Calories today", "$calories kcal")
                        SummaryCard("Water today", "$water ml")
                        SummaryCard("Exercise today", "$exercise min")

                        Text("Record your weight", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            draft, { draft = it },
                            label = { Text("Weight in kg") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                draft.toFloatOrNull()?.takeIf { it in 20f..500f }?.let {
                                    weight = "%.1f".format(java.util.Locale.US, it)
                                    prefs.edit().putString("weight", weight).apply()
                                    weights.add(WeightEntry(LocalDate.now().toString(), it))
                                    saveWeights()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save weight entry") }

                        HorizontalDivider()
                        Text("Weight history", fontWeight = FontWeight.Bold)
                        if (weights.isEmpty()) Text("Save your first weight to begin tracking.")
                        weights.asReversed().forEach {
                            Card {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(it.date)
                                    Text(
                                        "${"%.1f".format(java.util.Locale.US, it.kg)} kg",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            "Your entries are stored on this device. This version does not sync to an online account."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(
            Modifier.padding(14.dp),verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

           
