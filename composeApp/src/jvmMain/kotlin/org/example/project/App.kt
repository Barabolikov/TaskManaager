package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // 1. ІМПОРТ "ЛІНИВОГО" СПИСКУ
import androidx.compose.foundation.lazy.items // 2. ІМПОРТ функції 'items' для LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton// 3. ІМПОРТ стандартних іконок
// 4. ІМПОРТ іконки "Кошик"


import androidx.compose.material3.* // Ми імпортуємо багато компонентів Material 3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration // 5. ІМПОРТ для закреслення тексту
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

// 6. НАША МОДЕЛЬ ДАНИХ
// id - унікальний ключ, name - текст, isDone - стан прапорця
data class Task(
    val id: Int,
    val name: String,
    val isDone: Boolean
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        // 7. СТАН ДЛЯ ПОЛЯ ВВОДУ ТЕКСТУ
        var text by remember { mutableStateOf("") }

        // 8. СТАН ДЛЯ ЗБЕРІГАННЯ ID НАСТУПНОГО ЗАВДАННЯ
        var nextId by remember { mutableStateOf(1) }

        // 9. ГОЛОВНИЙ СТАН: СПИСОК ЗАВДАНЬ
        // Використовуємо 'mutableStateListOf' - Compose реагує на додавання/видалення
        val tasks = remember { mutableStateListOf<Task>() }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp), // Дамо відступи з усіх боків
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Мій список справ", style = MaterialTheme.typography.headlineMedium) // Заголовок

            Spacer(Modifier.height(16.dp))

            // 10. БЛОК ДЛЯ ДОДАВАННЯ НОВИХ ЗАВДАНЬ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Поле вводу займає весь доступний простір
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Нове завдання...") },
                    modifier = Modifier.weight(1f) // 'weight(1f)' каже "забери весь вільний простір"
                )

                Spacer(Modifier.width(8.dp)) // Відступ між полем і кнопкою

                Button(onClick = {
                    // 11. ЛОГІКА ДОДАВАННЯ
                    if (text.isNotBlank()) { // Додаємо, лише якщо текст не порожній
                        // Створюємо нове завдання
                        val newTask = Task(
                            id = nextId,
                            name = text,
                            isDone = false
                        )
                        tasks.add(newTask) // Додаємо його в наш стан-список
                        nextId++ // Збільшуємо лічильник ID
                        text = "" // Очищуємо поле вводу
                    }
                }) {
                    Text("Додати")
                }
            }

            Spacer(Modifier.height(16.dp))

            // 12. "ЛІНИВИЙ" СПИСОК ДЛЯ ВІДОБРАЖЕННЯ
            // Він рендерить лише ті елементи, які видно на екрані
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 13. Функція 'items' приймає наш список
                // 'key' - це оптимізація. Ми кажемо Compose, що id - це унікальний ключ.
                // Це допомагає йому правильно анімувати додавання/видалення.
                items(tasks, key = { it.id }) { task ->
                    // 14. Викликаємо наш власний Composable для одного рядка
                    TaskItem(
                        task = task,
                        // 15. Лямбда "onCheckedChange": що робити, коли натиснули Checkbox
                        onCheckedChange = { isChecked ->
                            // Знаходимо індекс завдання, яке змінилося
                            val index = tasks.indexOf(task)
                            if (index != -1) {
                                // ЗАМІНЮЄМО старий об'єкт на новий з оновленим 'isDone'
                                // Ми не можемо просто змінити 'task.isDone',
                                // бо стан реагує на заміну об'єктів.
                                tasks[index] = task.copy(isDone = isChecked)
                            }
                        },
                        // 16. Лямбда "onDeleteClick": що робити, коли натиснули "Видалити"
                        onDeleteClick = {
                            tasks.remove(task) // Просто видаляємо завдання зі списку
                        }
                    )
                    Divider() // Додаємо лінію-роздільник між завданнями
                }
            }
        }
    }
}

// 17. НАШ КОМПОНЕНТ ДЛЯ ОДНОГО РЯДКА СПИСКУ
@Composable
fun TaskItem(
    task: Task, // Завдання, яке малюємо
    onCheckedChange: (Boolean) -> Unit, // Функція, яку треба викликати при зміні Checkbox
    onDeleteClick: () -> Unit // Функція, яку треба викликати при видаленні
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
            // 18. Якщо завдання виконано - закреслюємо текст
            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
        )

        Spacer(Modifier.width(8.dp))

        // 19. КНОПКА-ІКОНКА ДЛЯ ВИДАЛЕННЯ
        IconButton(onClick = onDeleteClick) {
           Text("Видалити")
        }
    }
}