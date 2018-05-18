package com.unilol.comp4521.unilol.interfaces

import java.util.*


data class Comment (
    var id: String = "",
    var message: String = "",
    var user_id: String = "",
    var upvotes: Int = 0,
    var time: Date = Date()
)