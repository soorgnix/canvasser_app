package com.adlr.wjbvisits.presentation.visits

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date

class VisitSearchViewModel : ViewModel() {
    val searchQuery = mutableStateOf("")

    val mCalendar = Calendar.getInstance()
    var remYear = mutableStateOf(mCalendar.get(Calendar.YEAR))
    var remMonth = mutableStateOf(mCalendar.get(Calendar.MONTH))
    var remDay = mutableStateOf(mCalendar.get(Calendar.DAY_OF_MONTH))

    val defaultDate = Date(remYear.value - 1900, remMonth.value, remDay.value) // Note: year must be relative to 1900
    var dateQuery = mutableStateOf(com.google.firebase.Timestamp(defaultDate))

    val checkedQuery = mutableStateOf("Unchecked")

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setDateQuery(year: Int, month: Int, day: Int, date: Timestamp) {
        remYear.value = year
        remMonth.value = month
        remDay.value = day
        dateQuery.value = date
    }

    fun setCheckedQuery(query: String) {
        checkedQuery.value = query
    }
}