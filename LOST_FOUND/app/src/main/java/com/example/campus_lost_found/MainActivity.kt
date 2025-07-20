package com.example.campus_lost_found

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var fabAddItem: FloatingActionButton

    private val fragments = listOf(
        LostItemsFragment(),
        FoundItemsFragment(),
        MyReportsFragment()
    )

    private val tabTitles = listOf(
        R.string.lost_items,
        R.string.found_items,
        R.string.my_reports
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        fabAddItem = findViewById(R.id.fabAddItem)

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        // Set up ViewPager and TabLayout
        setupViewPager()
        setupListeners()

        // Check if user is logged in
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // TODO: Redirect to login activity or show login dialog
            // For now, just show a toast message
            MaterialAlertDialogBuilder(this)
                .setTitle("Authentication Required")
                .setMessage("Please sign in to use the Campus Lost & Found features.")
                .setPositiveButton("Sign In") { _, _ ->
                    // TODO: Launch sign in flow
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun setupViewPager() {
        // Set up ViewPager with fragments
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        // Connect TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(tabTitles[position])
        }.attach()
    }

    private fun setupListeners() {
        fabAddItem.setOnClickListener {
            showReportDialog()
        }
    }

    private fun showReportDialog() {
        val options = arrayOf("Report Lost Item", "Report Found Item")

        MaterialAlertDialogBuilder(this)
            .setTitle("What would you like to report?")
            .setItems(options) { _, which ->
                val isLostItem = which == 0
                navigateToReportScreen(isLostItem)
            }
            .show()
    }

    private fun navigateToReportScreen(isLostItem: Boolean) {
        val intent = Intent(this, ReportItemActivity::class.java).apply {
            putExtra("isLostItem", isLostItem)
        }
        startActivity(intent)
    }
}
