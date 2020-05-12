package com.mfrancetic.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        setupLoginView()
        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        sign_up_login_button.setOnClickListener(View.OnClickListener {

            val email = email_edit_text.text.toString()
            val password = password_edit_text.text.toString()

            if (!emailAndPasswordBlank(email, password)) {
                if (isLoginMode()) {
                    loginUser(email, password)
                } else {
                    val password2 = password2_edit_text.text.toString()
                    signUpUser(email, password, password2)
                }
            }
        })

        switch_to_sign_up_login_text_view.setOnClickListener(View.OnClickListener {
            if (isLoginMode()) {
                setupSignUpView()
            } else {
                setupLoginView()
            }
        })
    }

    private fun emailAndPasswordBlank(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, getString(R.string.email_password_empty), Toast.LENGTH_SHORT)
                .show()
            return true
        }
        return false
    }

    private fun signUpUser(email: String, password: String, password2: String) {
        if (password != password2) {
            Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT)
                .show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(task.result?.user, email)
                    Toast.makeText(this, getString(R.string.sign_up_success), Toast.LENGTH_SHORT)
                        .show()
                    goToUserListActivity()
                } else {
                    Toast.makeText(
                        baseContext, task.exception?.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT)
                        .show()
                    goToUserListActivity()
                } else {
                    Toast.makeText(
                        baseContext, task.exception?.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setupLoginView() {
        switch_to_sign_up_login_text_view.text = getString(R.string.switch_to_sign_up)
        sign_up_login_button.text = getString(R.string.login)
        password2_edit_text.visibility = View.GONE
    }

    private fun isLoginMode(): Boolean {
        return sign_up_login_button.text == getString(R.string.login)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToUserListActivity()
        }
    }

    private fun goToUserListActivity() {
        val goToUserListActivityIntent = Intent(this, UserListActivity::class.java)
        startActivity(goToUserListActivityIntent)
    }

    private fun setupSignUpView() {
        switch_to_sign_up_login_text_view.text = getString(R.string.switch_to_login)
        sign_up_login_button.text = getString(R.string.sign_up)
        password2_edit_text.visibility = View.VISIBLE
    }

    private fun addUserToDatabase(user: FirebaseUser?, email: String) {
        if (user != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child(Constants.USERS_TABLE_KEY).child(user.uid).child(Constants.EMAIL_ID_KEY)
                .setValue(email)
        }
    }
}