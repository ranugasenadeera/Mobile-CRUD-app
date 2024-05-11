package com.example.madexam4

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class EventsAdapter(private  var events: List<Events>, context: Context) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

        private val db: DatabaseHelper = DatabaseHelper(context)
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val locationTextView: TextView = itemView.findViewById(R.id.location)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.titleTextView.text = event.title
        holder.descriptionTextView.text = event.description
        holder.dateTextView.text = event.date
        holder.locationTextView.text = event.location

        holder.updateButton.setOnClickListener{
            val intent = Intent(holder.itemView.context, UpdateEvent::class.java).apply {
                putExtra("event_id", event.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            // Show confirmation dialog before deleting the event
            val alertDialogBuilder = AlertDialog.Builder(holder.itemView.context)
            alertDialogBuilder.setTitle("Confirm Deletion")
            alertDialogBuilder.setMessage("Are you sure you want to delete this event?")
            alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                // User confirmed deletion
                db.deleteEvent(event.id)
                refreshData(db.getAllEvents())
                Toast.makeText(holder.itemView.context, "Event deleted successfully", Toast.LENGTH_SHORT).show()
            }
            alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                // User cancelled deletion
                dialog.dismiss()
            }
            alertDialogBuilder.create().show()
        }
    }

    fun refreshData(newEvents: List<Events>){
        events = newEvents
        notifyDataSetChanged()
    }
}