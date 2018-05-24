/*
# COMP 4521 # IYER ARVIND   20218689    aiyer@connect.ust.hk
# COMP 4521 # RYAN BUDI     20214841    bryanaa@connect.ust.hk
# COMP 4521 # JI TONY WAI SUM 20217881  twsji@connect.ust.hk
*/
package com.unilol.comp4521.unilol.interfaces

import java.util.*


data class Comment (
    var id: String = "",
    var message: String = "",
    var user_id: String = "",
    var upvotes: Int = 0,
    var time: Date = Date()
)
