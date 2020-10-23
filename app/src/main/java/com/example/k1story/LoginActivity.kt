package com.example.k1story

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG="LoginActivity"
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Firebase Authentication
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            goPostsActivity()
        }

        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if(email.isBlank() || password.isBlank()) {
                btnLogin.isEnabled = true;
                Toast.makeText(this, "Email / Password cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task->
                btnLogin.isEnabled = true
                if(task.isSuccessful) {
                    Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show()
                    goPostsActivity()
                }
                else
                {
                    Log.e(TAG,"SignInWithEmail faild",task.exception)
                    Toast.makeText(this,"Authentication failed",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goPostsActivity(){
        Log.i(TAG,"goPostsActivity")
        val intent = Intent(this,PostsActivity::class.java)
        startActivity(intent)
        finish()
    }

}