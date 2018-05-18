package com.unilol.comp4521.unilol.interfaces

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.like.LikeButton
import com.like.OnLikeListener
import com.unilol.comp4521.unilol.R
import java.util.ArrayList

class CommentsListAdapter
(private val mContext: Context, private val mResource: Int, objects: ArrayList<Comment>, postId: String) : ArrayAdapter<Comment>(mContext, mResource, objects) {
    private var lastPosition = -1
    private val postId = postId

    /**
     * Holds variables in a View
     */
    private class ViewHolder {
        internal var comment: TextView? = null
        internal var author: TextView? = null
        internal var upvotes: TextView? = null
        internal var time: TextView? = null
        internal var mProgressBar: ProgressBar? = null
        internal var commentLikeButton: LikeButton? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val id = getItem(position)!!.id
        val comment = getItem(position)!!.message
        val author = getItem(position)!!.user_id
        val upvotes = getItem(position)!!.upvotes.toString() + " upvotes"
        val time = getItem(position)!!.time.toString()
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val requestUser = db.collection("users").document(currentUser.uid)
        val requestComment = db.collection("posts").document(postId)
                .collection("comments")
                .document(id)

        try {
            //create the view result for showing the animation
            val result: View

            //ViewHolder object
            val holder: ViewHolder

            if (convertView == null) {
                val inflater = LayoutInflater.from(mContext)
                convertView = inflater.inflate(mResource, parent, false)
                holder = ViewHolder()
                holder.comment = convertView!!.findViewById(R.id.comment) as TextView
                holder.author = convertView.findViewById(R.id.comment_author) as TextView
                holder.upvotes = convertView.findViewById(R.id.comment_upvotes) as TextView
                holder.time = convertView.findViewById(R.id.comment_time) as TextView
                holder.mProgressBar = convertView.findViewById(R.id.comment_progressbar) as ProgressBar
                holder.commentLikeButton = convertView.findViewById(R.id.comment_like_button) as LikeButton
                convertView.tag = holder


            } else {
                holder = convertView.tag as ViewHolder
                holder.mProgressBar!!.visibility = View.VISIBLE
            }


            lastPosition = position

            holder.comment!!.setText(comment)
            holder.author!!.setText(author)
            holder.upvotes!!.setText(upvotes)
            holder.time!!.setText(time.toString())

            // Get the comments liked from the current user and light the heart up
            requestUser.get().addOnCompleteListener({task ->
                if(task.isSuccessful) {
                    val user = task.result.toObject(User::class.java)
                    user!!.votes.comments.forEach {
                        key, value ->
                        if (key == id)
                            holder.commentLikeButton!!.isLiked = true
                    }
                }
                else{
                    Log.d(TAG, "Error collecting user! ${task.exception}")
                }
            })



            holder.mProgressBar!!.visibility = View.GONE

            holder.commentLikeButton!!.setOnLikeListener(object: OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    requestUser.get().addOnCompleteListener({task ->
                        if(task.isSuccessful) {
                            val user = task.result.toObject(User::class.java)
                            user!!.votes.comments[id] = 1

                            // Update the liked the comment to the server
                            requestUser.update("votes.comments", user.votes.comments).addOnCompleteListener({task_inner->
                                if(task_inner.isSuccessful){
                                    Log.d(TAG, "Added liked comment to the server")
                                }
                                else{
                                    Log.d(TAG, "Fail adding liked comment to the server")
                                }
                            })

                            // Add number of upvote of the given comment
                            getItem(position)!!.upvotes += 1

                            requestComment.update("upvotes", getItem(position)!!.upvotes).addOnCompleteListener({task_inner->
                                if(task_inner.isSuccessful){
                                    Log.d(TAG, "Added number of upvotes")
                                    holder.upvotes!!.setText(getItem(position)!!.upvotes.toString() + " upvotes")
                                }
                                else{
                                    Log.d(TAG, "Failed adding number of upvotes")
                                }
                            })
                        }
                        else{
                            Log.d(TAG, "Error collecting user! ${task.exception}")
                        }
                    })
                }

                override fun unLiked(p0: LikeButton?) {
                    requestUser.get().addOnCompleteListener({task ->
                        if(task.isSuccessful) {
                            val user = task.result.toObject(User::class.java)
                            user!!.votes.comments.remove(id)

                            // Remove the liked comment from the server
                            requestUser.update("votes.comments", user.votes.comments).addOnCompleteListener({task_inner->
                                if(task_inner.isSuccessful){
                                    Log.d(TAG, "Unliked comment from the server")
                                }
                                else{
                                    Log.d(TAG, "Failed unliking comment from the server")
                                }
                            })

                            // Add number of upvote of the given comment
                            getItem(position)!!.upvotes -= 1

                            requestComment.update("upvotes", getItem(position)!!.upvotes).addOnCompleteListener({task_inner->
                                if(task_inner.isSuccessful){
                                    Log.d(TAG, "Added number of upvotes")
                                    holder.upvotes!!.setText(getItem(position)!!.upvotes.toString() + " upvotes")
                                }
                                else{
                                    Log.d(TAG, "Failed adding number of upvotes")
                                }
                            })
                        }
                        else{
                            Log.d(TAG, "Error collecting user! ${task.exception}")
                        }
                    })
                }
            })
            return convertView

        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "getView: IllegalArgumentException: " + e.message)
            return convertView!!
        }

    }

    companion object {
        private val TAG = "CommentListAdapter"
    }

}