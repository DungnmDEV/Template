package com.percas.studio.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.percas.studio.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBannerTest.setOnClickListener {
            startActivity(Intent(this, BannerTestActivity::class.java))
        }
        binding.btnNativeTest.setOnClickListener {
            startActivity(Intent(this, NativeTestActivity::class.java))
        }
        binding.btnInterstitialTest.setOnClickListener {
            startActivity(Intent(this, InterstitialTestActivity::class.java))
        }
        binding.btnRewardTest.setOnClickListener {
            startActivity(Intent(this, RewardTestActivity::class.java))
        }
        binding.btnAppOpenTest.setOnClickListener {
            startActivity(Intent(this, AppOpenTestActivity::class.java))
        }
    }
}
