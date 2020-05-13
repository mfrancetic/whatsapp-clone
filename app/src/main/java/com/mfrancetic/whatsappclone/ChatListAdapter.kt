package com.mfrancetic.whatsappclone

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatListAdapter(private val context: Context, private val chatList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var chatMessageTextView: TextView = itemView.findViewById(R.id.chat_message_text_view)
        var chatCreatedAtTextView: TextView = itemView.findViewById(R.id.chat_created_at_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = chatList[position]
        val createdAt = DateTimeUtils.formatTime(chatMessage.createdAt)

        holder.chatMessageTextView.text = chatMessage.message
        holder.chatCreatedAtTextView.text = createdAt

        if (chatMessage.from == FirebaseAuth.getInstance().currentUser?.uid) {
            holder.chatMessageTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            holder.chatMessageTextView.setTextColor(ContextCompat.getColor(context, R.color.colorInverse))
        } else {
            holder.chatMessageTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight))
            holder.chatMessageTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryText))
        }
    }
}