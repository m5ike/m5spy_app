package com.myspy.android.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.systemcore.databinding.FragmentLogsBinding
import com.android.systemcore.databinding.ItemLogBinding
import com.myspy.android.ui.logs.models.LogEntry
import com.myspy.android.ui.logs.models.LogLevel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Logs Fragment
 * @author Michael KOJDL
 * @version 1.0.0
 *
 * Displays application logs with:
 * - Filtering by log level (DEBUG, INFO, WARNING, ERROR)
 * - Export functionality
 * - Real-time updates
 */
class LogsFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!

    private val logs = mutableListOf<LogEntry>()
    private val filteredLogs = mutableListOf<LogEntry>()
    private lateinit var adapter: LogsAdapter

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        loadSampleLogs()
    }

    private fun setupRecyclerView() {
        adapter = LogsAdapter(filteredLogs)
        binding.recyclerViewLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLogs.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnClearLogs.setOnClickListener {
            clearLogs()
        }

        binding.btnExportLogs.setOnClickListener {
            exportLogs()
        }

        // Filter chips
        binding.chipDebug.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.chipInfo.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.chipWarning.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.chipError.setOnCheckedChangeListener { _, _ -> applyFilters() }
    }

    private fun loadSampleLogs() {
        // Add some sample logs
        addLog(LogLevel.INFO, "Application started")
        addLog(LogLevel.DEBUG, "Loading settings from preferences")
        addLog(LogLevel.INFO, "API Base URL: ${com.android.systemcore.BuildConfig.API_BASE_URL}")
        addLog(LogLevel.INFO, "WebSocket URL: ${com.android.systemcore.BuildConfig.WSS_BASE_URL}")
        addLog(LogLevel.DEBUG, "Initializing modules")
        addLog(LogLevel.WARNING, "Some permissions not granted")
        addLog(LogLevel.INFO, "Service started successfully")
    }

    fun addLog(level: LogLevel, message: String) {
        val logEntry = LogEntry(
            level = level,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        logs.add(0, logEntry) // Add to beginning

        // Keep only last 500 logs
        if (logs.size > 500) {
            logs.removeAt(logs.size - 1)
        }

        applyFilters()
        Timber.tag("LogsFragment").d("Added log: $level - $message")
    }

    private fun applyFilters() {
        val showDebug = binding.chipDebug.isChecked
        val showInfo = binding.chipInfo.isChecked
        val showWarning = binding.chipWarning.isChecked
        val showError = binding.chipError.isChecked

        filteredLogs.clear()
        filteredLogs.addAll(logs.filter { log ->
            when (log.level) {
                LogLevel.DEBUG -> showDebug
                LogLevel.INFO -> showInfo
                LogLevel.WARNING -> showWarning
                LogLevel.ERROR -> showError
            }
        })

        adapter.notifyDataSetChanged()

        // Show/hide empty state
        binding.textEmptyLogs.visibility = if (filteredLogs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun clearLogs() {
        logs.clear()
        filteredLogs.clear()
        adapter.notifyDataSetChanged()
        binding.textEmptyLogs.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Logs cleared", Toast.LENGTH_SHORT).show()
    }

    private fun exportLogs() {
        // TODO: Implement export to file
        val logText = logs.joinToString("\n") { log ->
            "${dateFormat.format(Date(log.timestamp))} [${log.level}] ${log.message}"
        }

        Timber.i("Exporting ${logs.size} logs")
        Toast.makeText(requireContext(), "Export functionality coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * RecyclerView Adapter for logs
     */
    private inner class LogsAdapter(
        private val logs: List<LogEntry>
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<LogViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val binding = ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return LogViewHolder(binding)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            holder.bind(logs[position])
        }

        override fun getItemCount(): Int = logs.size
    }

    /**
     * ViewHolder for log items
     */
    private inner class LogViewHolder(
        private val binding: ItemLogBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(log: LogEntry) {
            binding.textLogLevel.text = log.level.toString()
            binding.textLogTime.text = dateFormat.format(Date(log.timestamp))
            binding.textLogMessage.text = log.message

            // Set color based on log level
            val color = when (log.level) {
                LogLevel.DEBUG -> 0xFF9E9E9E.toInt()
                LogLevel.INFO -> 0xFF4CAF50.toInt()
                LogLevel.WARNING -> 0xFFFFC107.toInt()
                LogLevel.ERROR -> 0xFFF44336.toInt()
            }

            binding.viewLevelIndicator.setBackgroundColor(color)
            binding.textLogLevel.setTextColor(color)
        }
    }
}
