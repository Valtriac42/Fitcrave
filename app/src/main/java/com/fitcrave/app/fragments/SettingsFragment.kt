package com.fitcrave.app.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fitcrave.app.BuildConfig
import com.fitcrave.app.R
import com.fitcrave.app.activities.WebViewActivity
import com.fitcrave.app.databinding.FragmentSettingsBinding
import com.fitcrave.app.update.UpdateManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rowWeb.setOnClickListener {
            startActivity(Intent(requireContext(), WebViewActivity::class.java))
        }
        binding.rowUpdate.setOnClickListener { checkForUpdate() }
    }

    private fun checkForUpdate() {
        binding.updateProgress.visibility = View.VISIBLE
        binding.tvUpdateStatus.text = getString(R.string.update_downloading) // "Downloading..." briefly while checking
        binding.tvUpdateStatus.text = "Checking…"
        lifecycleScope.launch {
            val latest = UpdateManager.fetchLatest()
            binding.updateProgress.visibility = View.GONE
            if (latest == null) {
                binding.tvUpdateStatus.text = getString(R.string.update_check_failed)
                return@launch
            }
            val newer = UpdateManager.isNewer(BuildConfig.VERSION_NAME, latest.versionName)
            if (!newer) {
                binding.tvUpdateStatus.text =
                    "${getString(R.string.update_up_to_date)} (v${BuildConfig.VERSION_NAME})"
                return@launch
            }
            binding.tvUpdateStatus.text =
                "${getString(R.string.update_available)}: ${latest.tagName}"
            promptDownload(latest)
        }
    }

    private fun promptDownload(info: com.fitcrave.app.update.ReleaseInfo) {
        val ctx = context ?: return
        val sizeMb = if (info.sizeBytes > 0) " (${String.format("%.1f", info.sizeBytes / 1024.0 / 1024.0)} MB)" else ""
        AlertDialog.Builder(ctx)
            .setTitle("${getString(R.string.update_available)} — ${info.tagName}$sizeMb")
            .setMessage(info.notes.take(400).ifBlank { "A new version of Fitcrave is available." })
            .setPositiveButton(R.string.update_install) { _, _ -> startDownload(info) }
            .setNegativeButton(R.string.update_later, null)
            .show()
    }

    private fun startDownload(info: com.fitcrave.app.update.ReleaseInfo) {
        val ctx = context ?: return
        binding.updateProgress.visibility = View.VISIBLE
        binding.tvUpdateStatus.text = getString(R.string.update_downloading)
        UpdateManager.enqueueDownload(ctx.applicationContext, info) { file ->
            // Receiver may fire on any thread; hop to main
            val act = activity ?: return@enqueueDownload
            act.runOnUiThread {
                if (_binding != null) {
                    binding.updateProgress.visibility = View.GONE
                }
                if (file == null) {
                    Toast.makeText(act, "Download failed", Toast.LENGTH_SHORT).show()
                    if (_binding != null) binding.tvUpdateStatus.text = "Download failed"
                } else {
                    if (_binding != null) binding.tvUpdateStatus.text = "Tap to install"
                    UpdateManager.launchInstall(act, file)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
