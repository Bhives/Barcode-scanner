package com.reproflex3.rplink.presentation.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.reproflex3.rplink.R
import com.reproflex3.rplink.databinding.FragmentScannerBinding

class ScannerFragment : Fragment() {

    private lateinit var binding: FragmentScannerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scannerPager.adapter = ScanPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.scannerPager, true, false) { tab, position ->
            when (position) {
                0 -> {
                    tab.customView = getTabCustomTextView(R.string.smart_scan)
                }
                1 -> {
                    tab.customView = getTabCustomTextView(R.string.qr_code)
                }
                else -> {
                    tab.customView = getTabCustomTextView(R.string.code_128)
                }
            }
        }.attach()
    }

    private fun getTabCustomTextView(textId: Int): TextView {
        val textView = TextView(requireContext()).apply {
            setTextAppearance(R.style.TextAppearance_GolosRegular_14_White)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setText(textId)
        }
        return textView
    }
}