package com.reproflex3.rplink.presentation.scan

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.digimarc.dis.interfaces.DISErrorListener
import com.digimarc.dis.interfaces.DISResultListener
import com.digimarc.dms.payload.Payload
import com.digimarc.dms.readers.BaseReader
import com.digimarc.dms.readers.ReaderResult
import com.digimarc.dms.resolver.ResolvedContent
import com.google.android.material.snackbar.Snackbar
import com.reproflex3.rplink.R
import com.reproflex3.rplink.databinding.FragmentBarcodeScanBinding
import com.reproflex3.rplink.presentation.MainViewModel
import com.reproflex3.rplink.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BarcodeScanFragment : Fragment() {

    private lateinit var binding: FragmentBarcodeScanBinding

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBarcodeScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeScannerStatus()
        observeScanSuccess()
        observeScanError()
    }

    private fun observeScannerStatus() {
        viewModel.shouldScan.observe(viewLifecycleOwner) {
            if (it) {
                requestToSetupDMSView()
            } else {
                stopDMSView()
            }
        }
    }

    private fun observeScanSuccess() {
        with(binding.scanBorder) {
            viewModel.scanResult.observe(viewLifecycleOwner) { scanResult ->
                if (scanResult.isNotBlank()) {
                    statusText.visibility = View.GONE
                    statusIcon.visibility = View.VISIBLE
                    scanFrame.strokeColor =
                        ContextCompat.getColor(requireContext(), R.color.green_border)
                    val snackbar = Snackbar.make(
                        binding.root,
                        getString(R.string.scan_result_message, scanResult),
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.show()
                    snackbar.addCallback(getSnackBarCallback())
                    scanFrame.strokeColor =
                        ContextCompat.getColor(requireContext(), R.color.green_border)
                } else {
                    statusText.visibility = View.VISIBLE
                    statusText.setText(R.string.scanning)
                    statusIcon.visibility = View.GONE
                    scanFrame.strokeColor = Color.WHITE
                }
            }
        }
    }

    private fun getSnackBarCallback() = object : Snackbar.Callback() {

        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            viewModel.toggleScanInitialization(true)
        }
    }

    private fun observeScanError() {
        with(binding.scanBorder) {
            viewModel.errorText.observe(viewLifecycleOwner) { error ->
                errorText.text = error
                if (error.isNotBlank()) {
                    statusText.visibility = View.INVISIBLE
                    statusIcon.visibility = View.INVISIBLE
                    errorText.visibility = View.VISIBLE
                    scanFrame.strokeColor =
                        ContextCompat.getColor(requireContext(), R.color.red_border)
                } else {
                    errorText.visibility = View.INVISIBLE
                    scanFrame.strokeColor = Color.WHITE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.scanBorder.statusText.setText(R.string.place_item)
        binding.childRootView.visibility = View.VISIBLE
        viewModel.resetValues()
        viewModel.toggleScannerType(true)
    }

    @SuppressLint("MissingPermission")
    private fun requestToSetupDMSView() {
        if (!permissionCheck()) {
            requestPermissions()
            return
        } else {
            setupDMSView()
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    private fun setupDMSView() {
        binding.dmsDetector.initialize(
            BaseReader.buildSymbologyMask(
                BaseReader.ImageSymbology.Image_1D_Code128,
                BaseReader.ImageSymbology.Image_Digimarc
            ),
            null,
            getDISListener(),
            getDISErrorListener()
        )
    }

    private fun stopDMSView() {
        binding.dmsDetector.uninitialize()
    }

    private fun onPermissionDenied(value: Int) {
        navigate(R.id.requestPermissionsFragment)
        viewModel.updatePermissionPreferences(value)
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher =
        activityResultLauncher(::recreateViews, ::onPermissionDenied)

    @SuppressLint("MissingPermission")
    private fun recreateViews() {
        binding.root.removeAllViews()
        binding.root.addView(binding.childRootView)
        setupDMSView()
    }

    private fun getDISListener(): DISResultListener = object : DISResultListener {

        override fun onImageResult(result: ReaderResult): List<Payload>? {
            viewModel.processScanResult(result.payloads?.find { it != null })
            return result.newPayloads
        }

        override fun onAudioResult(result: ReaderResult): List<Payload>? {
            return result.newPayloads
        }

        override fun onResolved(result: ResolvedContent) {}
    }

    private fun getDISErrorListener(): DISErrorListener =
        DISErrorListener { _, errorMessage ->
            val error = "Error: $errorMessage"
            viewModel.displayError(error)
        }

    override fun onPause() {
        super.onPause()
        binding.childRootView.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDMSView()
    }
}