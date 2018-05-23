package com.unilol.comp4521.unilol.interfaces

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.unilol.comp4521.unilol.R
import java.util.ArrayList

/**
 * Created by tonyji on 23/5/2018.
 */

class RelationListAdapter
(private val mContext: Context, private val mResource: Int, private val userIds: ArrayList<String>, private val clickListener: (Int) -> Unit) : ArrayAdapter<String>(mContext, mResource, userIds) {

    private class ViewHolder(view: View) {
        var imageView: ImageView = view.findViewById(R.id.profile_image) as ImageView
        var textView: TextView = view.findViewById(R.id.profile_fullname) as TextView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View
        var viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(mResource, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val db = FirebaseFirestore.getInstance()
        val userProfile = db.collection("users").document(userIds[position])
        userProfile.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result.toObject(User::class.java)!!
                val url = user.profilePictureUrl
                val fullname = user.fullName
                Picasso.get().load(url).into(viewHolder.imageView)
                viewHolder.textView.text = fullname
            }
        }
        view.setOnClickListener { clickListener(position) }
        return view
    }
}