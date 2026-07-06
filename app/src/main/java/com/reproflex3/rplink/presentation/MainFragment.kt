package com.reproflex3.rplink.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.reproflex3.rplink.R
import com.reproflex3.rplink.databinding.FragmentMainBinding
import com.reproflex3.rplink.utils.setIsVisibleBy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
    }

    private fun setupNavigation() {
        val destinations = intArrayOf(
            R.id.scannerFragment,
            R.id.accountFragment,
            R.id.requestPermissionsFragment
        )
        val navHorstFragment =
            childFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHorstFragment.findNavController()
        with(binding.bottomNavigationView) {
            navController.addOnDestinationChangedListener { _, destination, _ ->
                setIsVisibleBy { destinations.contains(destination.id) }
            }
            NavigationUI.setupWithNavController(this, navController)
        }
    }
}