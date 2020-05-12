package com.mfrancetic.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
    private var userEmail: String? = ""
    private var userKey: String? = ""
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    //    private var messageId = UUID.randomUUID().toString()
    private var chatId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        getDataFromIntent()
        getChatId()
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
                sendMessage(message)
            }
        })
    }

    private fun sendMessage(message: String) {
        val createdAt = Calendar.getInstance().time
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (currentUserUid != null && userKey != null && userEmail != null && currentUserEmail != null) {
            val messageMap: Map<String, String> = mapOf(
                Constants.MESSAGE_KEY to message,
                Constants.CREATED_AT to createdAt.toString(),
                Constants.FROM_KEY to currentUserUid.toString(),
                Constants.TO_KEY to userKey.toString()
            )

            if (chatId == "") {
                chatId = UUID.randomUUID().toString()
            }

            val chatsTable = database.child(Constants.CHATS_TABLE_KEY).child(chatId)
            chatsTable.child(Constants.PARTICIPANTS_KEY).child(Constants.PARTICIPANT_1_KEY)
                .setValue(currentUserUid)

            chatsTable.child(Constants.PARTICIPANTS_KEY).child(Constants.PARTICIPANT_2_KEY)
                .setValue(userKey!!)
//            chatsTable.child(Constants.PARTICIPANTS_KEY).child(userKey!!).push()

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
        if (currentUserUid != null) {
            if (chatId != "") {
                database.child(Constants.CHATS_TABLE_KEY).child(chatId)
                    .child(Constants.MESSAGES_KEY)
//                    .orderByChild(Constants.CREATED_AT)
                    .addChildEventListener(object : ChildEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                        }

                        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                        }

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
                    })
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(baseContext, chatList)
        chat_recycler_view.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        chat_recycler_view.layoutManager = layoutManager
    }

    private fun getDataFromIntent() {
        if (intent != null) {
            userEmail = intent.getStringExtra(Constants.USER_KEY)
            userKey = intent.getStringExtra(Constants.USER_KEY_KEY)
            title = getString(R.string.chat_with) + " " + userEmail
        }
    }

    private fun getChatId() {
        database.child(Constants.CHATS_TABLE_KEY)
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    if (p0.hasChild(Constants.PARTICIPANTS_KEY)) {
                        val participants = p0.child(Constants.PARTICIPANTS_KEY)
                        if (participants.child(Constants.PARTICIPANT_1_KEY).value == userKey ||
                            participants.child(Constants.PARTICIPANT_1_KEY).value == currentUserUid
                        ) {
                            if (participants.child(Constants.PARTICIPANT_2_KEY).value == userKey ||
                                    participants.child(Constants.PARTICIPANT_2_KEY).value == currentUserUid) {
                                chatId = p0.key.toString()
                                getChatListFromDatabase()
                            }
                        }
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }
            })
    }
}