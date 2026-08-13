package com.shalenmathew.movieflix.presentation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.backup.BackupWorker
import com.shalenmathew.movieflix.core.backup.RestoreWorker
import com.shalenmathew.movieflix.databinding.FragmentBackupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackupFragment : Fragment() {

    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                startBackupWorker(uri.toString())
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                startRestoreWorker(uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "movieflix_backup_${System.currentTimeMillis()}.json")
            }
            exportLauncher.launch(intent)
        }

        binding.btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            importLauncher.launch(intent)
        }
    }

    private fun startBackupWorker(uriString: String) {
        val data = Data.Builder().putString("URI", uriString).build()
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(data)
            .build()
        
        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueue(request)
        
        workManager.getWorkInfoByIdLiveData(request.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    Toast.makeText(requireContext(), getString(R.string.msg_export_success), Toast.LENGTH_SHORT).show()
                } else if (workInfo?.state == WorkInfo.State.FAILED) {
                    Toast.makeText(requireContext(), getString(R.string.msg_export_failed), Toast.LENGTH_SHORT).show()
                }
            }
            
        Toast.makeText(requireContext(), getString(R.string.msg_exporting_background), Toast.LENGTH_SHORT).show()
    }

    private fun startRestoreWorker(uriString: String) {
        val data = Data.Builder().putString("URI", uriString).build()
        val request = OneTimeWorkRequestBuilder<RestoreWorker>()
            .setInputData(data)
            .build()
            
        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueue(request)
        
        workManager.getWorkInfoByIdLiveData(request.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    Toast.makeText(requireContext(), getString(R.string.msg_restore_success), Toast.LENGTH_SHORT).show()
                } else if (workInfo?.state == WorkInfo.State.FAILED) {
                    Toast.makeText(requireContext(), getString(R.string.msg_restore_failed), Toast.LENGTH_SHORT).show()
                }
            }

        Toast.makeText(requireContext(), getString(R.string.msg_restoring_background), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}