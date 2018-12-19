package com.example.mahfuzkhandaker.simper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            val email = email_editText_login.text.toString()
            val password = password_editText_login.text.toString()
            Log.d("LoginActivity", "Email: " + email)
            Log.d("LoginActivity", "Password: $password ")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("LoginActivity", "Login success")

                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.d("LoginActivity", "SignInWithEmail: failure", it.exception)
                        finish()
                    }
                }


        }
        back_to_signUp_textView.setOnClickListener {
            finish()
        }
    }
}
