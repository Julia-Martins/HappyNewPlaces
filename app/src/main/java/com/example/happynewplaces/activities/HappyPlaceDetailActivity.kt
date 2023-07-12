package com.example.happynewplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happynewplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happynewplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private var binding: ActivityHappyPlaceDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetailModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel = intent.getParcelableExtra<HappyPlaceModel>(
                MainActivity.EXTRA_PLACE_DETAILS
            ) as HappyPlaceModel

            if (happyPlaceDetailModel != null){
                setSupportActionBar(binding?.toolDetailPlace)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.title = happyPlaceDetailModel.title

                binding?.toolDetailPlace?.setNavigationOnClickListener {
                    onBackPressed()
                }

                binding?.imgPlaceDetail?.setImageURI(Uri.parse(happyPlaceDetailModel.image))
                binding?.txtDescriptionDetail?.text = happyPlaceDetailModel.description
                binding?.txtLocationDetail?.text = happyPlaceDetailModel.location

                binding?.btnViewOnMapDetail?.setOnClickListener {
                    val intent = Intent(this, MapActivity::class.java)

                    intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)

                    startActivity(intent)
                }
            }
        }
    }
}