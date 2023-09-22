package com.orbys.demowhiteboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orbys.demowhiteboard.databinding.ActivityMainBinding
import com.skg.drawaccelerate.AccelerateManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AccelerateManager.instance.onCreate()

        binding.btnDraw.setOnClickListener {
            GlobalConfig.sMode = 0
        }

        binding.btnEraser.setOnClickListener {
            GlobalConfig.sMode = 1
        }
    }

    override fun onStart() {
        super.onStart()
        AccelerateManager.instance.onStart()
    }

    override fun onResume() {
        super.onResume()
        AccelerateManager.instance.onResume()
    }

    override fun onPause() {
        super.onPause()
        AccelerateManager.instance.onPause()
    }

    override fun onStop() {
        super.onStop()
        AccelerateManager.instance.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        AccelerateManager.instance.onDestroy()
    }
}