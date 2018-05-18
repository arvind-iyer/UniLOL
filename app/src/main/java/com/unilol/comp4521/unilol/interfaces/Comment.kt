package com.unilol.comp4521.unilol.interfaces

import java.util.*

class Comment(var comment: String?, var author: String?, var upvotes: Int?, var time: Date?) {

    override fun toString(): String {
        return "Comment{" +
                "comment='" + comment + '\''.toString() +
                ", author='" + author + '\''.toString() +
                ", upvotes='" + upvotes + '\''.toString() +
                ", date='" + time + '\''.toString() +
                '}'.toString()
    }
}