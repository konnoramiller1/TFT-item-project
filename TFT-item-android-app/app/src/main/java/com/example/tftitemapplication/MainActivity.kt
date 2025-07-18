package com.example.tftitemapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val databaseRepository = DatabaseRepository()
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputLayout: LinearLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var resultLayout: LinearLayout
    private lateinit var userInput: EditText
    private lateinit var loadButton: Button
    private lateinit var adapter: GridAdapter
    private lateinit var titleTextView: TextView
    private lateinit var gridLayoutManager: GridLayoutManager
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        titleTextView = findViewById(R.id.titleText)
        inputLayout = findViewById(R.id.inputLayout)
        loadingLayout = findViewById(R.id.loadingLayout)
        resultLayout = findViewById(R.id.resultsLayout)
        userInput = findViewById(R.id.userInput)
        loadButton = findViewById(R.id.loadButton)
        recyclerView = findViewById(R.id.recyclerView)
    }

    private fun setupRecyclerView(data: MutableMap<String, Double>) {
        // Set RecyclerView to be visible
        loadingLayout.visibility = View.GONE
        resultLayout.visibility = View.VISIBLE

        // Create sample data
        val items = createGridData(data.toList().sortedBy { it.second })

        // Create adapter with click listener
        adapter = GridAdapter(items) { item ->
            handleItemClick(item)
        }

        gridLayoutManager = GridLayoutManager(this, 1)

        // Set up RecyclerView
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        loadButton.setOnClickListener {
            searchQuery = userInput.text.toString().trim()
            if (searchQuery.isEmpty()) {
                Toast.makeText(this, "Please enter Riot API key", Toast.LENGTH_SHORT).show()
            } else {
                loadData(searchQuery)
            }
        }
    }

    private fun loadData(query: String) {
        lifecycleScope.launch {
            try {
                inputLayout.visibility = View.GONE
                loadingLayout.visibility = View.VISIBLE

                val data = when (query) {
                    "database" -> databaseRepository.fetchData()
                    else -> ItemDataFromAPI(query).getItemPlacements()
                }
                setupRecyclerView(data)

            } catch (e: Exception) {
                e.printStackTrace()
                titleTextView.text = getString(R.string.bad_input_text)
                loadingLayout.visibility = View.GONE
                inputLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun handleItemClick(item: GridItem) {
        Toast.makeText(this, "Clicked: ${item.itemName}", Toast.LENGTH_SHORT).show()
    }

    private fun createGridData(data: List<Pair<String, Double>>): List<GridItem> =
        data.map { (key, value) -> GridItem(TftItemsUtil.getReadableItemName(key), "%.3f".format(value), TftItemsUtil.getDrawable(key)) }

}
