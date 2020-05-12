package com.mfrancetic.whatsappclone;

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView;

class UserListAdapter(
    private val context: Context, private val users: MutableList<User>
) :

    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    class ViewHolder(
        private val context: Context, itemView: View,
        private val users: MutableList<User>
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var emailTextView: TextView = itemView.findViewById(R.id.user_email_text_view)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val goToChatActivityIntent = Intent(context, ChatActivity::class.java)
            goToChatActivityIntent.putExtra(Constants.USER_KEY, users[adapterPosition].userEmail)
            goToChatActivityIntent.putExtra(
                Constants.USER_KEY_KEY,
                users[adapterPosition].userUid
            )
            context.startActivity(goToChatActivityIntent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false)

        return ViewHolder(context, itemView, users)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        val emailTextView = holder.emailTextView
        emailTextView.text = user.userEmail
    }
}