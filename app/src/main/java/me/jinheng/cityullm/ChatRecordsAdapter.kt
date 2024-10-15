package me.jinheng.cityullm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatRecordsAdapter : ListAdapter<ChatRecord, ChatRecordsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(record: ChatRecord) {
            textView.text = record.title
            itemView.setOnClickListener {
                // TODO: Handle click to retrieve the chat
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatRecord>() {
        override fun areItemsTheSame(oldItem: ChatRecord, newItem: ChatRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatRecord, newItem: ChatRecord): Boolean {
            return oldItem == newItem
        }
    }
}