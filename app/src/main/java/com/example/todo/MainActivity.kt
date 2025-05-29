package com.example.todo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.databinding.ActivityMainBinding
import com.example.todo.databinding.DialogAddEditTaskBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import src.TaskAdapter
import java.io.File
import java.io.FileWriter
import java.util.*

data class Task(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String = "",
    var isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private val gson = Gson()
    private lateinit var jsonFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupJsonFile()
        setupRecyclerView()
        loadTasks()
        setupClickListeners()
    }

    private fun setupJsonFile() {
        jsonFile = File(filesDir, "tasks.json")
        if (!jsonFile.exists()) {
            jsonFile.createNewFile()
            saveTasksToJson(emptyList())
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = tasks,
            onTaskClick = { task: Task -> showEditTaskDialog(task) },
            onDeleteClick = { task: Task -> showDeleteConfirmation(task) },
            onCompleteToggle = { task: Task -> toggleTaskCompletion(task) }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun loadTasks() {
        try {
            if (jsonFile.length() > 0) {
                val jsonString = jsonFile.readText()
                val taskListType = object : TypeToken<List<Task>>() {}.type
                val loadedTasks: List<Task> = gson.fromJson(jsonString, taskListType) ?: emptyList()

                tasks.clear()
                tasks.addAll(loadedTasks.sortedByDescending { it.createdAt })
                taskAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "An error occurred while loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTasksToJson(taskList: List<Task>) {
        try {
            val jsonString = gson.toJson(taskList)
            FileWriter(jsonFile).use { writer ->
                writer.write(jsonString)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "An error occurred while saving tasks: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddEditTaskBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val title = dialogBinding.editTextTitle.text.toString().trim()
                val description = dialogBinding.editTextDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    addTask(title, description)
                } else {
                    Toast.makeText(this, "The task title can not be blank!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogBinding = DialogAddEditTaskBinding.inflate(layoutInflater)
        dialogBinding.editTextTitle.setText(task.title)
        dialogBinding.editTextDescription.setText(task.description)

        AlertDialog.Builder(this)
            .setTitle("Update Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                val title = dialogBinding.editTextTitle.text.toString().trim()
                val description = dialogBinding.editTextDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    updateTask(task, title, description)
                } else {
                    Toast.makeText(this, "Task title can not be blank!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure to delete '${task.title}' task?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addTask(title: String, description: String) {
        val newTask = Task(title = title, description = description)
        tasks.add(0, newTask)
        taskAdapter.notifyItemInserted(0)
        binding.recyclerViewTasks.scrollToPosition(0)
        saveTasksToJson(tasks)
        Toast.makeText(this, "Task Successfully Added", Toast.LENGTH_SHORT).show()
    }

    private fun updateTask(task: Task, newTitle: String, newDescription: String) {
        val index = tasks.indexOf(task)
        if (index != -1) {
            task.title = newTitle
            task.description = newDescription
            task.updatedAt = System.currentTimeMillis()
            taskAdapter.notifyItemChanged(index)
            saveTasksToJson(tasks)
            Toast.makeText(this, "Task Successfully Updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(task: Task) {
        val index = tasks.indexOf(task)
        if (index != -1) {
            tasks.removeAt(index)
            taskAdapter.notifyItemRemoved(index)
            saveTasksToJson(tasks)
            Toast.makeText(this, "Task Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleTaskCompletion(task: Task) {
        task.isCompleted = !task.isCompleted
        task.updatedAt = System.currentTimeMillis()
        val index = tasks.indexOf(task)
        if (index != -1) {
            taskAdapter.notifyItemChanged(index)
            saveTasksToJson(tasks)
        }
    }
}