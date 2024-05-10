package com.example.madexam4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.madexam4.databinding.ActivityEventBinding
import com.example.madexam4.databinding.ActivityMainBinding

class Event : AppCompatActivity() {

    private lateinit var binding: ActivityEventBinding
    private lateinit var db: DatabaseHelper
    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        eventsAdapter = EventsAdapter(db.getAllEvents(), this)

        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventsRecyclerView.adapter = eventsAdapter

        binding.addButton.setOnClickListener{
            val intent = Intent(this, AddEvent::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        eventsAdapter.refreshData(db.getAllEvents())
    }
}