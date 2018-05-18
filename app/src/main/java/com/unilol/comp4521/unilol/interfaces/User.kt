package com.unilol.comp4521.unilol.interfaces

/**
 * Created by arvind on 18/5/18.
 */

data class Votes(
        val posts: Map<String, Int>,
        val comments: Map<String, Int>
)

data class User (
        val id : String,
        val username: String,
        val profilePictureUrl: String,
        val posts: ArrayList<Post>?,
        val votes: Votes?,
        val email: String,
        val fullName: String
)

