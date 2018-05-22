package com.unilol.comp4521.unilol.interfaces

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.unilol.comp4521.unilol.R
import java.util.ArrayList

/**
 * Created by tonyji on 22/5/2018.
 */

class SettingsListAdapter
(private val mContext: Context, private val mResource: Int, private val objects: Array<String>, private val clickListener: (Int) -> Unit) : ArrayAdapter<String>(mContext, mResource, objects) {

    private class ViewHolder(view: View) {
        var textView: TextView = view.findViewById(R.id.settingsTextView) as TextView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View
        var viewHolder: ViewHolder

        var settingsName = getItem(position)!!
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(mResource, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.textView.text = objects[position]
        view.setOnClickListener { clickListener(position) }
        return view
    }
}