package com.example.soft1c.extension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflateLayout(@LayoutRes layoutId: Int, attachToParent: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToParent)
}