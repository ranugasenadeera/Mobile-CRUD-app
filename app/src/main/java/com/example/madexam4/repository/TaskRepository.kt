package com.example.madexam4.repository

import android.app.DownloadManager.Query
import com.example.madexam4.database.TaskDatabase
import com.example.madexam4.model.Task

class TaskRepository(private val db: TaskDatabase) {

    suspend fun insertTask(task: Task) = db.getTaskDao().insertTask(task)
    suspend fun deleteTask(task: Task) = db.getTaskDao().deleteTask(task)
    suspend fun updateTask(task: Task) = db.getTaskDao().updateTask(task)

    fun getAllTasks() = db.getTaskDao().getAllTasks()
    fun searchTask(query: String?) = db.getTaskDao().searchTask(query)
}