package com.orbys.demowhiteboard.ui.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.io.ByteArrayOutputStream
import java.util.Base64

object Util {

    /*
   punta fina -> 0.0012207404
   punta gorda -> 0.0042725913
   palma -> 0.027466659 (borrar)
    */

    const val finePointSize = 0.003
    const val thickPointSize = 0.01

    fun initDialogColor(context: Context, color:(Int)->Unit){
        ColorPickerDialog.Builder(context)
            .setTitle("ColorPicker Dialog")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(context.getString(android.R.string.ok),
                ColorEnvelopeListener { envelope, _ ->
                    color(envelope.color)
                })
            .setNegativeButton(
                context.getString(android.R.string.cancel)
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true) // the default value is true.
            .attachBrightnessSlideBar(true) // the default value is true.
            .show()
    }

    fun createBitmapToBase64String(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun createBitmapFromBase64String(base64String: String): Bitmap {
        val byteArray = Base64.getDecoder().decode(base64String)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}