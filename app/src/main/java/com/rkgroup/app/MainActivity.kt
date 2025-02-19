package com.rkgroup.app.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rkgroup.app.R
import com.rkgroup.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fileAdapter: FileAdapter

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addFiles(uris, contentResolver)
        }
    }

    // Permission launcher for Android 13+ (API 33+)
    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showFilePickerWithPermissions()
        } else {
            showMessage(getString(R.string.permissions_required))
        }
    }

    // Permission launcher for Android < 13
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showFilePickerWithPermissions()
        } else {
            showMessage(getString(R.string.permissions_required))
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
        }

        // Setup RecyclerView
        fileAdapter = FileAdapter(
            onDeleteClick = { fileItem -> viewModel.removeFile(fileItem) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Observe UI State
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUIState(state)
                }
            }
        }

        // Observe Selected Files
        viewModel.selectedFiles.observe(this) { files ->
            fileAdapter.submitList(files)
            binding.emptyStateGroup.isVisible = files.isEmpty()
            binding.uploadButton.isEnabled = files.isNotEmpty()
        }

        // Observe Upload Progress
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadProgress.collect { progressMap ->
                    fileAdapter.updateProgress(progressMap)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.addFilesButton.setOnClickListener {
            checkAndRequestPermissions()
        }

        binding.uploadButton.setOnClickListener {
            // TODO: Implement upload functionality
        }
    }

    private fun updateUIState(state: MainViewModel.UiState) {
        binding.progressBar.isVisible = state is MainViewModel.UiState.Uploading
        
        when (state) {
            is MainViewModel.UiState.Initial -> {
                binding.uploadButton.isEnabled = false
            }
            is MainViewModel.UiState.FilesSelected -> {
                binding.uploadButton.isEnabled = true
            }
            is MainViewModel.UiState.Uploading -> {
                binding.uploadButton.isEnabled = false
            }
            is MainViewModel.UiState.Error -> {
                showMessage(state.message)
            }
            is MainViewModel.UiState.Success -> {
                showMessage(getString(R.string.upload_success))
                viewModel.clearFiles()
            }
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

    private fun showFilePickerWithPermissions() {
        filePickerLauncher.launch("*/*")
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}