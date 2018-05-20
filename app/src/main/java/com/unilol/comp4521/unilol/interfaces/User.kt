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
        val posts: ArrayList<String>? = ArrayList(),
        val votes: Votes = Votes()
)

