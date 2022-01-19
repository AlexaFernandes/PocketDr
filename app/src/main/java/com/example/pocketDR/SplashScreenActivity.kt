package com.example.pocketDR

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import javax.security.auth.login.LoginException

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        

        val logoAnimation= AnimationUtils.loadAnimation(this, R.anim.logo_animation)

        val logoIcon: ImageView = findViewById(R.id.pdImageView)

        logoIcon.startAnimation(logoAnimation)

        val splashScreenTimeOut=4000
        //Activity to direct after SplashScreen
        //val homeIntent= Intent(this@SplashScreenActivity, LoginActivity::class.java)
        val homeIntent= Intent(this, _LoginActivity::class.java)

        Handler().postDelayed({
            startActivity(homeIntent)
            finish()
        }, splashScreenTimeOut.toLong())

    }
}