package com.reproflex3.rplink.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.reproflex3.rplink.R
import com.reproflex3.rplink.databinding.FragmentLoginBinding
import com.reproflex3.rplink.utils.navigate
import com.reproflex3.rplink.utils.popBackStack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLoadingState(true)
        observeErrorStatus()

        with(binding) {
            loginButton.setOnClickListener {
                setupLoadingState(true)
                viewModel.login(emailEditText.text.toString(), passwordEditText.text.toString())
            }
            forgotPassword.setOnClickListener {
                forgotPasswordAction()
            }
        }

        val navHorstFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.appNavHostFragment) as NavHostFragment

        viewModel.userLoggedIn.observe(viewLifecycleOwner) { userLoggedIn ->
            setupLoadingState(userLoggedIn)
            if (userLoggedIn) {
                navHorstFragment.popBackStack()
                navHorstFragment.navigate(R.id.mainFragment)
            }
        }
        viewModel.triggerForgotPasswordAction.observe(viewLifecycleOwner) { intent ->
            if (intent != null) {
                startActivity(intent)
            }
        }
    }

    private fun observeErrorStatus() {
        viewModel.emailErrorText.observe(viewLifecycleOwner) { error ->
            with(binding.emailInputLayout) {
                isErrorEnabled = error.isNullOrBlank()
                setError(error)
            }
        }
        viewModel.passwordErrorText.observe(viewLifecycleOwner) { error ->
            with(binding.passwordInputLayout) {
                isErrorEnabled = error.isNullOrBlank()
                setError(error)
            }
        }
    }

    private fun setupLoadingState(toggle: Boolean) {
        with(binding) {
            emailInputLayout.isEnabled = !toggle
            passwordInputLayout.isEnabled = !toggle
            if (toggle) {
                loginButton.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            } else {
                loginButton.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun forgotPasswordAction() {
        viewModel.goToForgotPassword()
    }
}