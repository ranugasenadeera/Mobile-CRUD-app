package com.example.madexam4.viewmodel

import android.app.Application
import android.app.DownloadManager.Query
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.madexam4.model.Task
import com.example.madexam4.repository.TaskRepository
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel(app: Application, private val taskRepository:TaskRepository): AndroidViewModel(app) {

    fun addTask(task: Task) =
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }

    fun deleteTask(task: Task) =
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }

    fun updateTask(task: Task) =
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }

    fun getAllTasks() = taskRepository.getAllTasks()

    fun searchTask(query: String?) =
        taskRepository.searchTask(query)

    fun getTasksByDate(date: String?) =
        taskRepository.getTasksByDate(date)
}