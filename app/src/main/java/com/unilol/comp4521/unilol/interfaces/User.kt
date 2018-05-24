/*
# COMP 4521 # IYER ARVIND   20218689    aiyer@connect.ust.hk
# COMP 4521 # RYAN BUDI     20214841    bryanaa@connect.ust.hk
# COMP 4521 # JI TONY WAI SUM 20217881  twsji@connect.ust.hk
*/
package com.unilol.comp4521.unilol.interfaces

/**
 * Created by arvind on 18/5/18.
 */

data class Votes(
        val posts : HashMap<String, Int> = HashMap<String, Int>(),
        val comments: HashMap<String, Int> = HashMap<String, Int>()
)

data class User (
        var id : String = "",
        var username: String = "",
        var profilePictureUrl: String = "",
        var email: String = "",
        var fullName: String = "",
        val posts: ArrayList<String> = ArrayList(),
        val votes: Votes = Votes(),
        var school: String = "HKUST",
        var status: String = "This guy is too lazy to write his status",
        val following: ArrayList<String> = ArrayList(),
        val followers: ArrayList<String> = ArrayList()
)
