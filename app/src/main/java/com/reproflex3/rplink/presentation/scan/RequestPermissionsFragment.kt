package com.reproflex3.rplink.presentation.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.reproflex3.rplink.R
import com.reproflex3.rplink.databinding.FragmentRequestPermissionsBinding
import com.reproflex3.rplink.utils.REQUIRED_PERMISSIONS
import com.reproflex3.rplink.utils.activityResultLauncher
import com.reproflex3.rplink.utils.navigate
import com.reproflex3.rplink.utils.permissionCheck
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestPermissionsFragment : Fragment() {

    private val viewModel by viewModels<RequestPermissionViewModel>()

    private lateinit var binding: FragmentRequestPermissionsBinding

    private var isPermissionPermanentlyDenied = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequestPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.permissionPermanentlyDenied.observe(viewLifecycleOwner) {
            isPermissionPermanentlyDenied = it
        }

        binding.permissionsButton.setOnClickListener {
            if (!permissionCheck()) {
                requestPermissions()
            }
        }
    }

    private fun requestPermissions() {
        if (isPermissionPermanentlyDenied) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts(PACKAGE, requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent)
        } else {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private val activityResultLauncher =
        activityResultLauncher({ navigate(R.id.scannerFragment) }, ::onPermissionDenied)

    private fun onPermissionDenied(value: Int) {
        viewModel.updatePermissionPreferences(value)
    }

    companion object {
        private const val PACKAGE = "package"
    }
}