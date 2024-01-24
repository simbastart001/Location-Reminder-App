package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentMainBinding
import com.udacity.project4.locationreminders.RemindersActivity

class MainFragment : Fragment() {
    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.authButton.text = getString(R.string.login_btn)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe the authentication state to adjust the UI accordingly
        observeAuthenticationState()

        binding.authButton.setOnClickListener { launchSignInFlow() }
        binding.resetPinButton.setOnClickListener {
            activity?.let {
                Snackbar.make(
                    requireView(),
                    "Please contact your Administrator",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(
                    TAG,
                    "User sign in successfully: ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                Snackbar.make(
                    requireView(),
                    "Welcome: ${FirebaseAuth.getInstance().currentUser?.displayName}",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    requireView(),
                    "Sign in unsuccessful ${response?.error?.errorCode}",
                    Snackbar.LENGTH_LONG
                ).show()
                Log.i(TAG, "Sign in unsuccessful: ${response?.error?.errorCode}")
            }
        }
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.authButton.text = getString(R.string.logout_button_text)
                    binding.authButton.setOnClickListener {
                        AuthUI.getInstance().signOut(requireContext())
                    }
                    //Start RemindersActivity after the user has successfully logged in
                    activity?.let {
                        val intent = Intent(it, RemindersActivity::class.java)
                        it.startActivity(intent)
                        it.finish()
                    }
                }

                else -> {
                    binding.authButton.text = getString(R.string.login_button_text)
                    binding.authButton.setOnClickListener { launchSignInFlow() }
                }
            }

        })

    }

    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        Log.i(TAG, "before start ActivityForResult")
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )


    }
}
