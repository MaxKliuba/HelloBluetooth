package com.maxclub.android.hellobluetooth.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.DrawableRes

class ArrayAdapterWithIcon(
    context: Context,
    private val resource: Int,
    private var items: List<Item>
) : ArrayAdapter<String>(context, resource, items.map { it.title }) {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        (layoutInflater.inflate(
            resource,
            parent,
            false
        ) as TextView).apply {
            text = getItem(position)
            setCompoundDrawablesWithIntrinsicBounds(
                items[position].drawableResId,
                0,
                0,
                0
            )
        }

    data class Item(@DrawableRes val drawableResId: Int, val title: String)
}