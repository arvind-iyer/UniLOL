package com.unilol.comp4521.unilol.interfaces

import java.util.*


data class Comment (
    var id: String = "",
    val message: String = "",
    val user_id: String = "",
    val upvotes: Int = 0,
    val time: Date = Date()
)