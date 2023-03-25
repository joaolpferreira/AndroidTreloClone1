package com.example.treloclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.example.treloclone.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        var btnSignUp: Button = findViewById(R.id.btn_sign_up)
        var btnSignIn: Button = findViewById(R.id.btn_sign_in)

        btnSignUp.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnSignIn.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}