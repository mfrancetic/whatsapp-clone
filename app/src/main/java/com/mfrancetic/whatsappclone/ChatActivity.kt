package com.mfrancetic.whatsappclone

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

class ChatActivity : AppCompatActivity() {

    private var adapter: ChatListAdapter? = null
    private var chatList: MutableList<ChatMessage> = mutableListOf()
    private var recipientEmail: String? = ""
    private var recipientUid: String? = ""
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private var chatId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        getDataFromIntent()
        findChat()
        setupRecyclerView()
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        send_message_button.setOnClickListener(View.OnClickListener {
            val message = write_message_edit_text.text.toString()
            if (message.isBlank()) {
                Toast.makeText(this, getString(R.string.message_empty), Toast.LENGTH_SHORT)
                    .show()
            } else {
                clearEditText()
                hideKeyboard()
                sendMessage(message)
            }
        })
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = if (currentFocus == null) View(this) else currentFocus

        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun clearEditText() {
        write_message_edit_text.text.clear()
    }

    private fun sendMessage(message: String) {
        val createdAt = Calendar.getInstance().time

        if (currentUserUid != null && recipientUid != null) {
            val messageMap: Map<String, String> = mapOf(
                Constants.MESSAGE_KEY to message,
                Constants.CREATED_AT to createdAt.toString(),
                Constants.FROM_KEY to currentUserUid.toString(),
                Constants.TO_KEY to recipientUid.toString()
            )

            val chatsTable = database.child(Constants.CHATS_TABLE_KEY).child(chatId)

            if (chatId == "") {
                chatId = UUID.randomUUID().toString()
                chatsTable.child(Constants.PARTICIPANTS_KEY).child(Constants.PARTICIPANT_1_KEY)
                    .setValue(currentUserUid)
                chatsTable.child(Constants.PARTICIPANTS_KEY).child(Constants.PARTICIPANT_2_KEY)
                    .setValue(recipientUid!!)
            }

            chatsTable
                .child(Constants.MESSAGES_KEY)
                .push().setValue(messageMap).addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.message_sent), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }
    }

    private fun getChatListFromDatabase() {
        if (chatId != "") {
            database.child(Constants.CHATS_TABLE_KEY).child(chatId)
                .child(Constants.MESSAGES_KEY)
                .orderByChild(Constants.CREATED_AT)
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                        if (p0.hasChildren()) {
                            val createdAt = p0.child(Constants.CREATED_AT).value as String
                            val from = p0.child(Constants.FROM_KEY).value as String
                            val message = p0.child(Constants.MESSAGE_KEY).value as String
                            val to = p0.child(Constants.TO_KEY).value as String

                            chatList.add(
                                ChatMessage(from, to, message, createdAt)
                            )
                            adapter?.notifyDataSetChanged()
                        }
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    }

                })
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(baseContext, chatList)
        chat_recycler_view.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        chat_recycler_view.layoutManager = layoutManager
        layoutManager.scrollToPosition(chatList.size - 1)
    }

    private fun getDataFromIntent() {
        if (intent != null) {
            recipientEmail = intent.getStringExtra(Constants.USER_KEY)
            recipientUid = intent.getStringExtra(Constants.USER_KEY_KEY)
            title = getString(R.string.chat_with) + " " + recipientEmail
        }
    }

    private fun findChat() {
        database.child(Constants.CHATS_TABLE_KEY)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    if (p0.hasChild(Constants.PARTICIPANTS_KEY)) {
                        val participants = p0.child(Constants.PARTICIPANTS_KEY)
                        val participant1 = participants.child(Constants.PARTICIPANT_1_KEY).value
                        val participant2 = participants.child(Constants.PARTICIPANT_2_KEY).value

                        if (participant1 == recipientUid || participant1 == currentUserUid) {
                            if (participant2 == recipientUid || participant2 == currentUserUid) {
                                chatId = p0.key.toString()
                                getChatListFromDatabase()
                            }
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }
            })
    }
}