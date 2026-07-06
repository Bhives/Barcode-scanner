package com.reproflex3.rplink.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.reproflex3.rplink.R
import com.reproflex3.rplink.databinding.ActivityMainBinding
import com.reproflex3.rplink.utils.navigate
import com.reproflex3.rplink.utils.popBackStack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        val navHorstFragment =
            supportFragmentManager.findFragmentById(R.id.appNavHostFragment) as NavHostFragment

        viewModel.loggingOut.observe(this) {
            if (it) {
                navHorstFragment.popBackStack()
                navHorstFragment.navigate(R.id.loginFragment)
            }
        }

        viewModel.logoutMessage.observe(this) { logoutMessage ->
            if (logoutMessage.isNotBlank()) {
                Toast.makeText(this, logoutMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}