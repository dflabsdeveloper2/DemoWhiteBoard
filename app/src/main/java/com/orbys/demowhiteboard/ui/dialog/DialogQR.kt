package com.orbys.demowhiteboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.orbys.demowhiteboard.data.api.RetrofitClient
import com.orbys.demowhiteboard.data.api.model.ImageDataInfo
import com.orbys.demowhiteboard.data.api.model.QrDataJson
import com.orbys.demowhiteboard.data.api.model.ResponseQR
import com.orbys.demowhiteboard.databinding.DialogQrBinding
import com.orbys.demowhiteboard.ui.core.Helper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DialogQR(context: Context, private var listBitmap:List<Bitmap>):Dialog(context) {

    //TODO: añadir viewmodel?

    private lateinit var binding:DialogQrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pbLoading.isVisible = true
        binding.ivQR.isVisible = false
        binding.tvTerms.isVisible = false

        binding.tvTerms.setOnClickListener {
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(com.orbys.demowhiteboard.core.Util.legalTerms)
            )
            context.startActivity(urlIntent)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val dateTime =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val password = ""

            val encryption = password.isNotEmpty()

            val list = mutableListOf<ImageDataInfo>()

            listBitmap.forEach {
                list.add(ImageDataInfo(Helper.createBitmapToBase64String(it),dateTime))
            }

            //Json del qr para subirlo al servidor
            val data =
                QrDataJson(
                    encryption = encryption,
                    password = if (password.isEmpty()) null else password.toInt(),
                    images = list.toTypedArray()
                )

            var urlImage: ResponseQR? = null
            try {
                urlImage = RetrofitClient.serviceQR.saveImages(data)
            } catch (e: Exception) {
                withContext(Dispatchers.Main){
                    binding.pbLoading.isVisible = false
                    binding.tvTerms.isVisible = true
                    Toast.makeText(context, "Error: $e", Toast.LENGTH_SHORT).show()
                }
            }
            val bitmap = urlImage?.let {
                getQrCodeBitmap(it.url)
            }

            withContext(Dispatchers.Main){
                binding.pbLoading.isVisible = false
                binding.ivQR.isVisible = true
                binding.tvTerms.isVisible = true
                binding.ivQR.setImageBitmap(bitmap)
            }
        }
    }

    private fun getQrCodeBitmap(url:String): Bitmap {
        val size = 512 //pixels
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 }
        val bits = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}
