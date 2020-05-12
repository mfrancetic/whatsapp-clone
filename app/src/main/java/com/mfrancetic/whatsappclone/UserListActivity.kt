package com.mfrancetic.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_user_list.*

class UserListActivity : AppCompatActivity() {

    private var userListAdapter: UserListAdapter? = null
    private var users: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        setupRecyclerView()
        getUsersFromDatabase()
    }

    private fun getUsersFromDatabase() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        val database = FirebaseDatabase.getInstance().reference
        database.child(Constants.USERS_TABLE_KEY)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val userUid = p0.key.toString()
                    val userEmail = p0.child(Constants.EMAIL_ID_KEY).value.toString()

                    if (userUid != currentUserUid) {
                        users.add(User(p0.key.toString(), userEmail))
                        userListAdapter?.notifyDataSetChanged()
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                    val userUid = p0.key.toString()

                    if (userUid != currentUserUid) {
                        for (user in users) {
                            if (user.userUid == userUid) {
                                users.remove(user)
                                userListAdapter?.notifyDataSetChanged()
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
            })
    }

    private fun setupRecyclerView() {
        userListAdapter = UserListAdapter(this, users)
        users_list_recycler_view.layoutManager = LinearLayoutManager(this)
        users_list_recycler_view.adapter = userListAdapter

        val decoration = DividerItemDecoration(
            this,
            LinearLayoutManager.VERTICAL
        )
        users_list_recycler_view.addItemDecoration(decoration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.logout) {
            logoutUser()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, getString(R.string.logout_successful), Toast.LENGTH_SHORT)
            .show()
        val goBackToMainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(goBackToMainActivityIntent)
    }
}