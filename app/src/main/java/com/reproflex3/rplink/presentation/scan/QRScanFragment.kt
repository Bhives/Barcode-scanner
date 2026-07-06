package com.reproflex3.rplink.presentation.scan

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.reproflex3.rplink.R
import com.reproflex3.rplink.databinding.FragmentQrScanBinding
import com.reproflex3.rplink.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException


@AndroidEntryPoint
class QRScanFragment : Fragment() {

    private lateinit var binding: FragmentQrScanBinding

    private val viewModel by viewModels<QRScanViewModel>()

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrScanBinding.inflate(inflater, container, false)
        binding.qrScanView.visibility = View.VISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseDetectorsAndSources()
        observeScanResultLink()
        observeScanError()
        observeScanResult()
    }

    private fun observeScanResultLink() {
        with(binding.scanBorder) {
            viewModel.scanResultLink.observe(viewLifecycleOwner) { scanResultLink ->
                if (scanResultLink.isNotBlank()) {
                    statusText.visibility = View.GONE
                    statusIcon.visibility = View.VISIBLE
                    scanFrame.strokeColor =
                        ContextCompat.getColor(requireContext(), R.color.green_border)
                    val bundle = Bundle()
                    bundle.putString(SCAN_RESULT_LINK, scanResultLink)
                    navigate(R.id.scanResultFragment, bundle)
                    viewModel.resetLink()
                } else {
                    statusText.visibility = View.VISIBLE
                    statusText.setText(R.string.scanning)
                    statusIcon.visibility = View.GONE
                    scanFrame.strokeColor = Color.WHITE
                }
            }
        }
    }

    private fun observeScanResult() {
        with(binding.scanBorder) {
            viewModel.scanResult.observe(viewLifecycleOwner) { scanResult ->
                if (scanResult.isNotBlank()) {
                    statusText.visibility = View.GONE
                    statusIcon.visibility = View.VISIBLE
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
            viewModel.allowToScan(true)
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
                    viewModel.allowToScan(true)
                } else {
                    errorText.visibility = View.INVISIBLE
                    scanFrame.strokeColor = Color.WHITE
                }
            }
        }
    }

    private fun initialiseDetectorsAndSources() {
        barcodeDetector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(FORMAT_QR_CODE)
            .build()
        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
            .setRequestedPreviewSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            .setAutoFocusEnabled(true)
            .build()
        binding.qrScanView.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    requestToSetupQRView()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
        viewModel.setupProcessor(barcodeDetector)
    }

    override fun onResume() {
        super.onResume()
        binding.scanBorder.statusText.setText(R.string.place_item)
        viewModel.allowToScan(true)
    }

    @SuppressLint("MissingPermission")
    private fun requestToSetupQRView() {
        if (!permissionCheck()) {
            requestPermissions()
            return
        } else {
            cameraSource.start(binding.qrScanView.holder)
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher =
        activityResultLauncher(::requestToSetupQRView, ::onPermissionDenied)

    private fun onPermissionDenied(value: Int) {
        navigate(R.id.requestPermissionsFragment)
        viewModel.updatePermissionPreferences(value)
    }

    override fun onPause() {
        super.onPause()
        stopQRView()
    }

    override fun onStop() {
        super.onStop()
        stopQRView()
    }

    private fun stopQRView() {
        cameraSource.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.release()
    }

    companion object {
        private const val DISPLAY_WIDTH = 1920
        private const val DISPLAY_HEIGHT = 1080
        private const val SCAN_RESULT_LINK = "SCAN_RESULT_LINK"
    }
}