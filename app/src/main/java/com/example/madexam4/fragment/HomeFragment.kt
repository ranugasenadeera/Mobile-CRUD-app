package com.example.madexam4.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.madexam4.Home
import com.example.madexam4.R
import com.example.madexam4.adapter.TaskAdapter
import com.example.madexam4.databinding.FragmentHomeBinding
import com.example.madexam4.model.Task
import com.example.madexam4.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener, MenuProvider {

    private var homeBinding: FragmentHomeBinding? = null
    private val binding get() = homeBinding!!

    private lateinit var tasksViewModel : TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    private var menu: Menu? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        tasksViewModel = (activity as Home).taskViewModel
        setupHomeRecyclerView()

        // Set up click listener for the floating action button to navigate to the AddTaskFragment
        binding.addTaskFab.setOnClickListener{
            it.findNavController().navigate(R.id.action_homeFragment_to_addTaskFragment)
        }
    }

    private fun updateUI(task: List<Task>?){
        if(task != null){
            if(task.isNotEmpty()){
                binding.emptyNotesImage.visibility = View.GONE
                binding.homeRecyclerView.visibility = View.VISIBLE
            }else{
                binding.emptyNotesImage.visibility = View.VISIBLE
                binding.homeRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun setupHomeRecyclerView(){
        taskAdapter = TaskAdapter()
        binding.homeRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = taskAdapter
        }

        activity?.let {
            tasksViewModel.getAllTasks().observe(viewLifecycleOwner){ task ->
                taskAdapter.differ.submitList(task)
                updateUI(task)
            }
        }
    }

    private fun searchTask(query: String) {
        val searchQuery = "%$query%"
        tasksViewModel.searchTask(searchQuery).observe(viewLifecycleOwner, Observer { tasks ->
            taskAdapter.differ.submitList(tasks)
            updateUI(tasks)
        })
    }

    private fun filterTasksByDate(date: String) {
        // Extract the selected date (day/month/year) from the input string (e.g., "28/5/2024")
        val dateParts = date.split("/")
        val selectedDay = dateParts[0].toInt()
        val selectedMonth = dateParts[1].toInt()
        val selectedYear = dateParts[2].split(" ")[0].toInt() // Extract year part and parse to integer

        // Filter tasks based on the date part only (ignoring the time)
        tasksViewModel.getAllTasks().observe(viewLifecycleOwner) { tasks ->
            val filteredTasks = tasks.filter { task ->
                // Split task's date and time components
                val taskDateParts = task.taskDate.split(" | ")[0].split("/")
                if (taskDateParts.size < 3) {
                    return@filter false // Handle unexpected date format
                }

                try {
                    val taskDay = taskDateParts[0].toInt()
                    val taskMonth = taskDateParts[1].toInt()
                    val taskYear = taskDateParts[2].split(" ")[0].toInt() // Extract year part and parse to integer

                    // Compare the extracted date parts to match the selected date
                    taskDay == selectedDay && taskMonth == selectedMonth && taskYear == selectedYear
                } catch (e: NumberFormatException) {
                    // Handle parsing errors (e.g., invalid integer format)
                    false
                }
            }

            // Update the RecyclerView with the filtered tasks
            taskAdapter.differ.submitList(filteredTasks)
            updateUI(filteredTasks)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            searchTask(it)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        homeBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)
        this.menu = menu

        val searchItem = menu.findItem(R.id.searchMenu)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(this)

        searchItem.setOnMenuItemClickListener {
            val backMenuItem = menu.findItem(R.id.backMenuItem)
            backMenuItem.isVisible = false

            false // Return false to allow further handling of the menu item click
        }

        // Initially hide the back button
        val backMenuItem = menu.findItem(R.id.backMenuItem)
        backMenuItem.isVisible = false
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        val backMenuItemId = R.id.backMenuItem
        val searchMenuItemId = R.id.searchMenu

        when (item.itemId) {
            R.id.filterDateMenu -> {
                // Show the back button when Filter by Date is selected
                val backMenuItem = menu?.findItem(backMenuItemId)
                backMenuItem?.isVisible = true

                val searchMenuItem = menu?.findItem(searchMenuItemId)
                searchMenuItem?.isVisible = false

                // Handle filter date action here
                try {
                    showDatePickerDialog()
                } catch (e: Exception) {
                    // Handle any exceptions that occur during date picker dialog display
                    e.printStackTrace()
                }
                return true
            }
            backMenuItemId -> {
                val intent = Intent(context, Home::class.java)
                startActivity(intent)
            }
        }

        return false
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = dateFormat.format(selectedDate.time)
                Log.d("HomeFragment", "Selected date: $dateString")
                filterTasksByDate(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

}