package com.mindorks.ridesharing.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mindorks.ridesharing.R

object MapUtils {

    fun getCarBitmap(context: Context): Bitmap {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }
}