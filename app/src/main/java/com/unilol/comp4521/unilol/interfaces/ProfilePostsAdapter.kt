package com.unilol.comp4521.unilol.interfaces

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso
import android.content.Context

/**
 * Created by tonyji on 22/5/2018.
 */

class ProfilePostsAdapter(private val context: Context, var posts: ArrayList<Post>, private val clickListener: (Post) -> Unit): BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var imageView: ImageView
        if (convertView == null) {
            val padding = 1
            val length: Int = (context.resources.displayMetrics.widthPixels - 10) / 3
            imageView = ImageView(context)
            imageView.setPadding(padding, padding, padding, padding)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.layoutParams = ViewGroup.LayoutParams(length, length)
            imageView.setOnClickListener {
                clickListener(posts[position])
            }
        } else {
            imageView = convertView as ImageView
        }

        Picasso.get().load(posts[position].url).into(imageView)
        return imageView
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int = posts.size
}