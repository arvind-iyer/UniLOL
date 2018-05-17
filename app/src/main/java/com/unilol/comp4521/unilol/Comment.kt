package com.unilol.comp4521.unilol

import com.google.firebase.Timestamp

class Comment(var comment: String?, var author: String?, var upvotes: Int?, var time: String?) {

    override fun toString(): String {
        return "Comment{" +
                "comment='" + comment + '\''.toString() +
                ", author='" + author + '\''.toString() +
                ", upvotes='" + upvotes + '\''.toString() +
                ", date='" + time + '\''.toString() +
                '}'.toString()
    }
}