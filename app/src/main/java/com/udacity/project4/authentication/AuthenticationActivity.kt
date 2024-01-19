package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

class AuthenticationActivity : AppCompatActivity() {

    private val SIGN_IN_RESULT_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        // Check if the user is authenticated otherwise launch sign in
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startRemindersActivity()
        } else {
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.AppTheme) // Set theme
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    private fun startRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Handle the sign in response
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                startRemindersActivity()
            } else {
                // Sign in failed; handle the error
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Sign in unsuccessful: ${response?.error?.errorCode}",
                    Snackbar.LENGTH_LONG
                ).show()

            }
        }
    }
}