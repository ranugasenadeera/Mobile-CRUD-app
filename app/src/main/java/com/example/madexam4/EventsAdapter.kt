package com.example.madexam4

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventsAdapter(private  var events: List<Events>, context: Context) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val locationTextView: TextView = itemView.findViewById(R.id.location)
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

    }

    fun refreshData(newEvents: List<Events>){
        events = newEvents
        notifyDataSetChanged()
    }
}