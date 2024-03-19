package com.adlr.wjbvisits.presentation.customer

import com.google.firebase.Timestamp

data class CustomerModel(
    val name: String = "",
    val address: String = "",
    val pic: String = "",
    val phone: String = "",
    val location: String = "",
    val user_id: String = "",
    val isAdded: Boolean = false,
    val note: String = "",
    val addedDate: Timestamp? = null,
    val updateDate: Timestamp? = null
)
