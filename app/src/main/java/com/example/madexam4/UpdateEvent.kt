package com.example.madexam4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.madexam4.databinding.ActivityUpdateEventBinding

class UpdateEvent : AppCompatActivity() {

    private  lateinit var binding: ActivityUpdateEventBinding
    private lateinit var db:DatabaseHelper
    private var eventId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        eventId = intent.getIntExtra("event_id", -1)
        if (eventId == -1) {
            finish()
            return
        }

        val event = db.getEventByID(eventId)
        binding.updateTitle.setText(event.title)
        binding.updateDescription.setText(event.description)
        binding.updateDate.setText(event.date)
        binding.updateLocation.setText(event.location)

        binding.updateSaveButton.setOnClickListener{
            val newTitle = binding.updateTitle.text.toString()
            val newDescription = binding.updateDescription.text.toString()
            val newDate = binding.updateDate.text.toString()
            val newLocation = binding.updateLocation.text.toString()

            val updatedEvent = Events(eventId, newTitle, newDescription, newDate, newLocation)
            db.updateEvent(updatedEvent)
            finish()
            Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()

        }
    }
}