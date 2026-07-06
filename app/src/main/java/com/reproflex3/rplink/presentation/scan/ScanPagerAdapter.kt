package com.reproflex3.rplink.presentation.scan

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ScanPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = PAGES_NUMBER

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SmartScanFragment()
            1 -> QRScanFragment()
            else -> BarcodeScanFragment()
        }
    }

    companion object {
        private const val PAGES_NUMBER = 3
    }
}