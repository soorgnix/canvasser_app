package com.adlr.wjbvisits.presentation.customer

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CustomerSearchViewModel : ViewModel() {
    val searchQuery = mutableStateOf("")

    val addedQuery = mutableStateOf("all")

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setAddedQuery(query: String) {
        addedQuery.value = query
    }
}