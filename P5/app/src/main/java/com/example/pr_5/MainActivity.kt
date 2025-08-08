package com.example.pr_5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize data
        val myDataset = DataSource().loadAffirmations()

        // Set up top section RecyclerView with horizontal layout
        try {
            val topRecyclerView = findViewById<RecyclerView>(R.id.topRecyclerView)
            topRecyclerView?.apply {
                layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = AffirmationAdapter(this@MainActivity, myDataset)
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Set up bottom section RecyclerView with grid layout
        val bottomRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        bottomRecyclerView?.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 1)
            adapter = AffirmationAdapter(this@MainActivity, myDataset)
            setHasFixedSize(true)
        }
    }
}