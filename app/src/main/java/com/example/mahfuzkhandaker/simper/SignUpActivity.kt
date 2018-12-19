package com.example.mahfuzkhandaker.simper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

class SignUpActivity : AppCompatActivity() {
    companion object {
        val TAG = "SignUpActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        button_signUp.setOnClickListener {
           userSignUp()
        }
        have_an_account_textView.setOnClickListener {
            Log.d(TAG, "Trying start LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        signUp_photo_button.setOnClickListener {
            Log.d(TAG, "Select photo for sign up")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedProfilePicUri: Uri ? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(TAG, "Try to upload photo")
            selectedProfilePicUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedProfilePicUri)

            selectProfilePic_imageView_signUp.setImageBitmap(bitmap)
            signUp_photo_button.alpha = 0f

          //  val bitmapDrawable = BitmapDrawable(bitmap)
          //  signUp_photo_button.setBackgroundDrawable(bitmapDrawable)

        }
    }
    private fun userSignUp(){
        //  val username = userName_editText_signUp.text.toString()
        val email = email_editText_signUp.text.toString()
        val password = password_editText_signUp.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Email: " + email)
        Log.d(TAG, "Password: $password")

        // Firebase Authentication to Sign Up with email & password

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(TAG, "Successfully created User: ${it.result!!.user.uid}")

                uploadProfilePic()
            }
            .addOnFailureListener {
                Log.d(TAG, "User created failed: ${it.message}")
            }
    }
    private fun uploadProfilePic(){
        if (selectedProfilePicUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedProfilePicUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully upload Profile Picture: ${it.metadata!!.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }
    private fun saveUserToFirebaseDatabase(profilePicUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, userName_editText_signUp.text.toString(), profilePicUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully saved user to FirebaseDatabase")

                // start LatestMessagesActivity
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
       }
    }
    @Parcelize
    class User(val uid: String, val username: String, val profilePicUrl: String): Parcelable {
        constructor(): this("","","")
    }
