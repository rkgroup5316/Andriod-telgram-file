package com.rkgroup.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rkgroup.app.R
import com.rkgroup.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // File picker launcher with improved error handling
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        when {
            uris.isNullOrEmpty() -> {
                showMessage(getString(R.string.no_files_selected))
            }
            uris.isNotEmpty() -> {
                binding.progressContainer.visibility = View.VISIBLE
                viewModel.processFilesInBackground(uris, contentResolver)
            }
        }
    }

    // Permission launcher for Android 13+ (API 33+)
    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.all { it.value } -> {
                showFilePickerSilently()
            }
            permissions.any { !it.value } -> {
                showPermissionDeniedDialog()
            }
        }
    }

    // Permission launcher for Android < 13
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            isGranted -> showFilePickerSilently()
            else -> showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.app_name)
        }

        // Configure progress indicator
        binding.progressIndicator.apply {
            isIndeterminate = false
            max = 100
            progress = 0
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: MainViewModel.UiState) {
        when (state) {
            is MainViewModel.UiState.Initial -> {
                updateProgressVisibility(false)
            }
            is MainViewModel.UiState.Progress -> {
                updateProgress(state.current, state.total)
            }
            is MainViewModel.UiState.Success -> {
                handleSuccess()
            }
            is MainViewModel.UiState.Error -> {
                handleError(state.message)
            }
        }
    }

    private fun updateProgress(current: Int, total: Int) {
        binding.progressContainer.visibility = View.VISIBLE
        binding.progressIndicator.apply {
            progress = ((current.toFloat() / total) * 100).toInt()
            visibility = View.VISIBLE
        }
        binding.progressText.text = getString(R.string.progress_format, current, total)
    }

    private fun updateProgressVisibility(show: Boolean) {
        binding.progressContainer.visibility = if (show) View.VISIBLE else View.GONE
        binding.progressIndicator.visibility = if (show) View.VISIBLE else View.GONE
        if (!show) {
            binding.progressText.text = ""
        }
    }

    private fun handleSuccess() {
        updateProgressVisibility(false)
        showMessage(getString(R.string.upload_success))
    }

    private fun handleError(message: String) {
        updateProgressVisibility(false)
        showError(message)
    }

    private fun setupClickListeners() {
        binding.fab.setOnClickListener {
            checkAndRequestPermissions()
        }

        binding.cancelButton.setOnClickListener {
            viewModel.cancelUploads()
            updateProgressVisibility(false)
            showMessage(getString(R.string.upload_cancelled))
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            storagePermissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun showFilePickerSilently() {
        filePickerLauncher.launch("*/*")
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_rationale)
            .setPositiveButton(R.string.retry) { _, _ ->
                checkAndRequestPermissions()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                viewModel.retryFailedUploads()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelUploads()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}