package com.example.madexam4.fragment

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.example.madexam4.Home
import com.example.madexam4.R
import com.example.madexam4.databinding.FragmentAddTaskBinding
import com.example.madexam4.model.Task
import com.example.madexam4.viewmodel.TaskViewModel
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class AddTaskFragment : Fragment(R.layout.fragment_add_task), MenuProvider {

    // Binding to access views in the layout
    private var addTaskBinding: FragmentAddTaskBinding? = null
    private val binding get() = addTaskBinding!!

    // ViewModel for task operations
    private lateinit var tasksViewModel: TaskViewModel
    private lateinit var addTaskView: View

    private var taskTitle: String? = null
    private var taskDate: String? = null

    // Inflate the layout and set up binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        addTaskBinding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Setup the view once it is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the menu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Get the ViewModel from the Home activity
        tasksViewModel = (activity as Home).taskViewModel
        addTaskView = view

        //Set up click listener for date EditText
        binding.addTaskDate.setOnClickListener {
            if (!binding.addTaskDate.isFocusable) {
                showDatePicker()
            }
        }
    }

    // Show date picker dialog
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
                binding.addTaskDate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
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
                binding.addTaskDate.append("  |  $selectedHour:$selectedMinute")

                // Schedule the notification and work request
                createWorkRequest(binding.addTaskTitle.text.toString(), calculateDelayInSeconds(selectedDate))
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
            .setInputData(workDataOf(
                "title" to "Reminder",
                "message" to message
            ))
            .build()

        WorkManager.getInstance(requireContext()).enqueue(myWorkRequest)
    }

    // Schedule a reminder using AlarmManager
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
            Toast.makeText(context, "Unable to schedule alarm: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        private const val REQUEST_CODE_SET_ALARM_PERMISSION = 1001
    }

    // Check and request necessary permissions for setting alarms
    private fun checkAndRequestPermissions() {
        val permission = Manifest.permission.SET_ALARM
        val permissionGranted = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            requestPermissions(arrayOf(permission), REQUEST_CODE_SET_ALARM_PERMISSION)
        } else {
            // Permission already granted, proceed with alarm setup
            taskTitle?.let { taskDate?.let { it1 -> scheduleReminder(it, it1) } }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_SET_ALARM_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with alarm setup
                    taskTitle?.let { taskDate?.let { it1 -> scheduleReminder(it, it1) } }
                } else {
                    // Permission denied, handle accordingly (e.g., show a message)
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            // Handle other permission request results if needed
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    // Save the task to the database
    private fun saveTask(view: View){
        val taskTitle = binding.addTaskTitle.text.toString().trim()
        val taskDesc = binding.addTaskDesc.text.toString().trim()
        val taskDate = binding.addTaskDate.text.toString().trim()

        if (taskTitle.isNotEmpty()){
            val task = Task(0, taskTitle, taskDesc, taskDate)
            tasksViewModel.addTask(task)

            Toast.makeText(addTaskView.context, "Event added successfully", Toast.LENGTH_SHORT).show()
            view.findNavController().popBackStack(R.id.homeFragment, false)
        }else{
            Toast.makeText(addTaskView.context, "Please enter task title", Toast.LENGTH_SHORT).show()
        }
    }

    // Inflate the menu
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_add_task, menu)
    }

    // Handle menu item selection
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.saveMenu -> {
                saveTask(addTaskView)
                true
            }
            else -> false
        }
    }

    // Clean up the binding to prevent memory leaks
    override fun onDestroy() {
        super.onDestroy()
        addTaskBinding = null
    }
}