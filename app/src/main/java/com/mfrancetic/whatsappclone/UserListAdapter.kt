package com.mfrancetic.whatsappclone;

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kotlinx.android.synthetic.main.activity_main.*

class UserListAdapter(
    private val context: Context, private val userEmails: MutableList<String>,
    private val userKeys: MutableList<String>
) :

    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    class ViewHolder(
        private val context: Context, itemView: View,
        private val userEmails: MutableList<String>,
    private val userKeys: MutableList<String>): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var emailTextView: TextView = itemView.findViewById(R.id.user_email_text_view)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val goToChatActivityIntent = Intent(context, ChatActivity::class.java)
            goToChatActivityIntent.putExtra(Constants.USER_KEY, userEmails[adapterPosition])
            goToChatActivityIntent.putExtra(Constants.USER_KEY_KEY, userKeys[adapterPosition])
            context.startActivity(goToChatActivityIntent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false)

        return ViewHolder(context, itemView, userEmails, userKeys)
    }

    override fun getItemCount(): Int {
        return userEmails.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val email = userEmails[position]

        val emailTextView = holder.emailTextView
        emailTextView.text = email
    }
}