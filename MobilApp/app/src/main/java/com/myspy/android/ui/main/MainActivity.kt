package com.myspy.android.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.systemcore.R
import com.android.systemcore.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.myspy.android.ui.logs.LogsFragment
import com.myspy.android.ui.registration.RegistrationFragment
import com.myspy.android.ui.settings.SettingsFragment
import com.myspy.android.ui.test.TestFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Tab-based UI
 * @author Michael KOJDL
 * @version 1.0.0
 *
 * Displays main application window with 4 tabs:
 * - Settings: Server URLs, stealth mode, auto-start, system hooks
 * - Logs: All application logs with filtering
 * - Test: Manual testing of API, modules, and data upload
 * - Registration: Device registration details and permissions
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val tabTitles = arrayOf(
        "Settings",
        "Logs",
        "Test",
        "Registration"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Disable swiping between tabs (optional - can be enabled if desired)
        binding.viewPager.isUserInputEnabled = true
    }

    /**
     * ViewPager2 Adapter for managing fragments
     */
    private inner class ViewPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SettingsFragment()
                1 -> LogsFragment()
                2 -> TestFragment()
                3 -> RegistrationFragment()
                else -> SettingsFragment()
            }
        }
    }
}
