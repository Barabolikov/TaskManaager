package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

// 1. ІМПОРТИ ДЛЯ РОБОТИ З ФАЙЛАМИ
import java.io.File
import kotlin.math.max // Допоміжна функція

// 2. ІМПОРТИ ДЛЯ СЕРІАЛІЗАЦІЇ (JSON)
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


// 3. АНОТАЦІЯ ДЛЯ СЕРІАЛІЗАЦІЇ
// Це каже бібліотеці, що цей клас можна конвертувати в JSON
@Serializable
data class Task(
    val id: Int,
    val name: String,
    val isDone: Boolean
)

// 4. ВИЗНАЧАЄМО НАШ ФАЙЛ ДЛЯ ЗБЕРЕЖЕННЯ
// "tasks.json" буде створено в тій самій папці, де запускається .jar файл
private val storageFile = File("tasks.json")

// 5. НАЛАШТОВУЄМО ПАРСЕР JSON
private val json = Json { prettyPrint = true } // prettyPrint робить JSON читабельним

@Composable
@Preview
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("") }
        var nextId by remember { mutableStateOf(1) } // Буде оновлено при завантаженні
        val tasks = remember { mutableStateListOf<Task>() }

        // 6. ЕФЕКТ ЗАВАНТАЖЕННЯ (SIDE-EFFECT)
        // 'LaunchedEffect(Unit)' виконується ОДИН РАЗ при старті App
        LaunchedEffect(Unit) {
            try {
                if (storageFile.exists()) {
                    // 7. Читаємо файл
                    val jsonString = storageFile.readText()
                    // 8. Конвертуємо JSON-рядок назад у список об'єктів Task
                    val loadedTasks = json.decodeFromString<List<Task>>(jsonString)
                    // 9. Додаємо завантажені завдання в наш стан
                    tasks.addAll(loadedTasks)

                    // 10. Оновлюємо лічильник 'nextId', щоб уникнути дублікатів
                    val maxId = loadedTasks.maxOfOrNull { it.id } ?: 0
                    nextId = maxId + 1
                }
            } catch (e: Exception) {
                // Обробка помилки, якщо файл пошкоджений
                println("Помилка завантаження завдань: ${e.message}")
            }
        }

        // 11. ЕФЕКТ ЗБЕРЕЖЕННЯ (SIDE-EFFECT)
        // 'LaunchedEffect(tasks.toList())' виконується КОЖНОГО РАЗУ,
        // коли вміст 'tasks' змінюється (додавання, видалення, зміна isDone)
        LaunchedEffect(tasks.toList()) {
            try {
                // 12. Конвертуємо наш список 'tasks' у JSON-рядок
                val jsonString = json.encodeToString(tasks.toList())
                // 13. Записуємо цей рядок у файл
                storageFile.writeText(jsonString)
            } catch (e: Exception) {
                // Обробка помилки, якщо файл не вдається записати
                println("Помилка збереження завдань: ${e.message}")
            }
        }

        // --- ВЕСЬ ІНШИЙ UI ЗАЛИШАЄТЬСЯ БЕЗ ЗМІН ---
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Мій список справ", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Нове завдання...") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Button(onClick = {
                    if (text.isNotBlank()) {
                        val newTask = Task(
                            id = nextId,
                            name = text,
                            isDone = false
                        )
                        tasks.add(newTask)
                        nextId++
                        text = ""
                    }
                }) {
                    Text("Додати")
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onCheckedChange = { isChecked ->
                            val index = tasks.indexOf(task)
                            if (index != -1) {
                                tasks[index] = task.copy(isDone = isChecked)
                            }
                        },
                        onDeleteClick = {
                            tasks.remove(task)
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

// Компонент TaskItem залишається БЕЗ ЗМІН
@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isDone,
            onCheckedChange = onCheckedChange
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = task.name,
            modifier = Modifier.weight(1f),
            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
        )

        Spacer(Modifier.width(8.dp))

        IconButton(onClick = onDeleteClick) {
            Text("Видалити")
        }
    }
}