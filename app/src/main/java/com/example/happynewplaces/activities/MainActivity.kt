package com.example.happynewplaces.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.happynewplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolMainPlace)

        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)

            startActivity(intent)
        }
    }
}

//https://github.com/Karumi/Dexter