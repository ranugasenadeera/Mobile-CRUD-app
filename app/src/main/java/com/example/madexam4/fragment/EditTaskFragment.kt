package com.example.madexam4.fragment

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.madexam4.Home
import com.example.madexam4.R
import com.example.madexam4.databinding.FragmentEditTaskBinding
import com.example.madexam4.model.Task
import com.example.madexam4.viewmodel.TaskViewModel
import java.util.Calendar
import java.util.concurrent.TimeUnit

class EditTaskFragment : Fragment(R.layout.fragment_edit_task), MenuProvider {

    private var editTaskBinding: FragmentEditTaskBinding? = null
    private val binding get() = editTaskBinding!!

    private lateinit var tasksViewModel: TaskViewModel
    private lateinit var currentTask: Task

    private val args: EditTaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        editTaskBinding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        tasksViewModel = (activity as Home).taskViewModel
        currentTask = args.task!!

        //Set up click listener for date EditText
        binding.editTaskDate.setOnClickListener {
            if (!it.isFocusable) {
                showDatePicker()
            }
        }

        binding.editTaskTitle.setText((currentTask.taskTitle))
        binding.editTaskDesc.setText((currentTask.taskDesc))
        binding.editTaskDate.setText((currentTask.taskDate))

        binding.editTaskFab.setOnClickListener{
            val taskTitle = binding.editTaskTitle.text.toString().trim()
            val taskDesc = binding.editTaskDesc.text.toString().trim()
            val taskDate = binding.editTaskDate.text.toString().trim()

            if (taskTitle.isNotEmpty()){
                val task = Task(currentTask.id, taskTitle, taskDesc, taskDate)
                tasksViewModel.updateTask(task)
                view.findNavController().popBackStack(R.id.homeFragment, false)
            }else{
                Toast.makeText(context, "Please enter task title", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }
                binding.editTaskDate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                showTimePicker(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    // Show time picker dialog
    private fun showTimePicker(selectedDate: Calendar) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedDate.set(Calendar.MINUTE, selectedMinute)
                selectedDate.set(Calendar.SECOND, 0)
                selectedDate.set(Calendar.MILLISECOND, 0)

                val taskTimeInMillis = selectedDate.timeInMillis
                binding.editTaskDate.append("  |  $selectedHour:$selectedMinute")

                // Schedule the notification and work request
                createWorkRequest(binding.editTaskTitle.text.toString(), calculateDelayInSeconds(selectedDate))
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    // Calculate the delay in seconds from the current time to the selected time
    private fun calculateDelayInSeconds(selectedDate: Calendar): Long {
        val userSelectedDateTime = selectedDate.timeInMillis / 1000L
        val todayDateTime = Calendar.getInstance().timeInMillis / 1000L
        return userSelectedDateTime - todayDateTime
    }

    // Create a work request to trigger the notification
    private fun createWorkRequest(message: String, timeDelayInSeconds: Long) {
        val myWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                "title" to "Reminder",
                "message" to message
            )
            )
            .build()

        WorkManager.getInstance(requireContext()).enqueue(myWorkRequest)
    }

    private fun scheduleReminder(taskTitle: String, taskDate: String) {
        val context = requireContext().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("title", taskTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateParts = taskDate.split("/")
        val year = dateParts[2].toInt()
        val month = dateParts[1].toInt() - 1
        val day = dateParts[0].toInt()

        val calendar = Calendar.getInstance().apply {
            set(year, month, day, 8, 0) // Set the reminder time (e.g., 8:00 AM)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canScheduleExact = alarmManager.canScheduleExactAlarms()
                if (canScheduleExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Handle case where exact alarms cannot be scheduled
                    // For example, show a message to the user or use a different approach
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Handle SecurityException if unable to set exact alarms
            e.printStackTrace()
            // For example, show a message to the user indicating the app needs additional permissions
            Toast.makeText(context, "Unable to schedule alarm: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(){
        activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Delete Task")
                setMessage("Do you want to delete this task?")
                setPositiveButton("Delete"){_,_ ->
                    currentTask?.let { taskTitle ->
                        cancelReminder(taskTitle)
                        tasksViewModel.deleteTask(taskTitle)
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                        view?.findNavController()?.popBackStack(R.id.homeFragment, false)
                        }
                }
                setNegativeButton("Cancel", null)
            }.create().show()
        }
    }

    private fun cancelReminder(taskTitle: Task) {
        val intent = Intent(requireContext(), ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_edit_task, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.deleteMenu -> {
                deleteTask()
                true
            }

            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editTaskBinding = null
    }
}