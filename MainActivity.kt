package com.fitlifepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FitLifeApp() }
    }
}

@Composable
fun FitLifeApp() {
    val nav = rememberNavController()
    var calories by remember { mutableIntStateOf(0) }
    var water by remember { mutableIntStateOf(0) }
    var exercise by remember { mutableIntStateOf(0) }
    var weight by remember { mutableStateOf("79.0") }
    var target by remember { mutableIntStateOf(1900) }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF176B4D),
        secondary = androidx.compose.ui.graphics.Color(0xFF4F7D68),
        background = androidx.compose.ui.graphics.Color(0xFFF6F8F6)
    )) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    listOf(
                        Triple("Home", "home", Icons.Default.Home),
                        Triple("Food", "food", Icons.Default.Restaurant),
                        Triple("Activity", "activity", Icons.Default.FitnessCenter),
                        Triple("Progress", "progress", Icons.Default.ShowChart)
                    ).forEach { item ->
                        NavigationBarItem(
                            selected = false,
                            onClick = { nav.navigate(item.second) { launchSingleTop = true } },
                            icon = { Icon(item.third, contentDescription = item.first) },
                            label = { Text(item.first) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
                composable("home") { HomeScreen(calories, target, water, exercise, weight,
                    onFood = { nav.navigate("food") }, onWater = { water += it },
                    onExercise = { exercise += it }, onWeight = { weight = it }) }
                composable("food") { FoodScreen(calories, onAdd = { calories += it }, onTarget = { target = it }) }
                composable("activity") { ActivityScreen(exercise, onAdd = { exercise += it }) }
                composable("progress") { ProgressScreen(weight, onWeight = { weight = it }, calories, water, exercise) }
            }
        }
    }
}

@Composable
fun HomeScreen(calories: Int, target: Int, water: Int, exercise: Int, weight: String,
               onFood: () -> Unit, onWater: (Int) -> Unit, onExercise: (Int) -> Unit, onWeight: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("FitLife Pro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(LocalDate.now().toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Card {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Daily calorie budget", style = MaterialTheme.typography.titleMedium)
                Text("$calories / $target kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { (calories.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text("${(target - calories).coerceAtLeast(0)} kcal remaining")
                Button(onClick = onFood, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Log food") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Water", "$water ml", "Target: 2,000 ml", Modifier.weight(1f))
            MetricCard("Exercise", "$exercise min", "Today", Modifier.weight(1f))
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Quick actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onWater(250) }, modifier = Modifier.weight(1f)) { Text("+250 ml water") }
                    OutlinedButton(onClick = { onExercise(10) }, modifier = Modifier.weight(1f)) { Text("+10 min workout") }
                }
                Text("Current weight: $weight kg")
                Text("Tip: log consistently. Weekly trends matter more than one day.")
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun FoodScreen(calories: Int, onAdd: (Int) -> Unit, onTarget: (Int) -> Unit) {
    var foodName by remember { mutableStateOf("") }
    var foodCalories by remember { mutableStateOf("150") }
    var newTarget by remember { mutableStateOf("1900") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Food diary", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Today's logged calories: $calories kcal")
        OutlinedTextField(foodName, { foodName = it }, label = { Text("Food or meal name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(foodCalories, { foodCalories = it.filter(Char::isDigit) }, label = { Text("Calories (kcal)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { val amount = foodCalories.toIntOrNull(); if (foodName.isNotBlank() && amount != null && amount > 0) { onAdd(amount); foodName = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Add food") }
        Divider()
        Text("Daily calorie target", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(newTarget, { newTarget = it.filter(Char::isDigit) }, label = { Text("Target kcal") }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { newTarget.toIntOrNull()?.takeIf { it in 500..10000 }?.let(onTarget) }, modifier = Modifier.fillMaxWidth()) { Text("Save target") }
        Text("Starter version: food entries are held in memory. Persistent food history and nutrition database are planned next.")
    }
}

@Composable
fun ActivityScreen(exercise: Int, onAdd: (Int) -> Unit) {
    var minutes by remember { mutableStateOf("20") }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Activity", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Exercise logged today: $exercise minutes")
        OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit) }, label = { Text("Workout duration (minutes)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { minutes.toIntOrNull()?.takeIf { it in 1..600 }?.let(onAdd) }, modifier = Modifier.fillMaxWidth()) { Text("Log workout") }
        Text("Calories burned are not estimated in this starter screen. A proper estimate needs activity type, duration, and user details.")
    }
}

@Composable
fun ProgressScreen(weight: String, onWeight: (String) -> Unit, calories: Int, water: Int, exercise: Int) {
    var draft by remember(weight) { mutableStateOf(weight) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Today's summary")
        MetricCard("Calories logged", "$calories kcal", "Food intake")
        MetricCard("Water", "$water ml", "Hydration")
        MetricCard("Exercise", "$exercise min", "Activity")
        OutlinedTextField(draft, { draft = it }, label = { Text("Current weight (kg)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { draft.toFloatOrNull()?.takeIf { it in 20f..500f }?.let { onWeight("%.1f".format(it)) } }, modifier = Modifier.fillMaxWidth()) { Text("Save weight") }
        Text("Weekly charts, long-term history, and persistent storage will be added in the next implementation phase.")
    }
}
