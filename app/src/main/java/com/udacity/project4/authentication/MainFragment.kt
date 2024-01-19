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
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.authButton.text = getString(R.string.login_btn)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the authentication state to adjust the UI accordingly
        observeAuthenticationState()

        binding.authButton.setOnClickListener {
            if (viewModel.authenticationState.value == LoginViewModel.AuthenticationState.AUTHENTICATED) {
                AuthUI.getInstance().signOut(requireContext())
            } else {
                launchSignInFlow()
            }
        }

        binding.createAccountButton.setOnClickListener {
            launchSignInFlow() // The same sign in flow includes account creation
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response =
                IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                startRemindersActivity()
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
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
                }

                else -> {
                    binding.authButton.text =
                        getString(R.string.login_button_text)
                    binding.authButton.setOnClickListener { launchSignInFlow() }
                }
            }
        })
    }

    private fun launchSignInFlow() {
        Log.i(TAG, "Launching sign-in flow")
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    private fun startRemindersActivity() {
        val intent = Intent(requireContext(), RemindersActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
