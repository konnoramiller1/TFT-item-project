package com.example.tftitemapplication

data class GridItem(
    val itemName: String,
    val placement: String,
    val imageResId: Int = R.drawable.tft_item_debugbase,
    val category: String = ""
)