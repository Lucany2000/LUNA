package com.luna.main

import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.luna.data.Song
import com.luna.utils.QueryTable
import com.luna.utils.RecycleViewAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchAlgoActivity: AppCompatActivity() {

    private lateinit var songAdapter: RecycleViewAdapter<Song>
    private lateinit var artistAdapter: RecycleViewAdapter<String>
    private lateinit var albumAdapter: RecycleViewAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_engine)

        val query = QueryTable(this)

        val searchView = findViewById<SearchView>(R.id.searchBar)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(input: String?): Boolean {
                // Handle search submission (e.g., API call, database query)
                return true
            }

            override fun onQueryTextChange(input: String?): Boolean {
                // Handle text change (e.g., filter a list dynamically)
                if (!input.isNullOrBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val readOnlyDB = query.readOnlyMode()
                        val queryResult = query.searchAlgo(readOnlyDB, input)
                    }
                }

                return true
            }
        })

    }
}