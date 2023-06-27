package com.example.happynewplaces.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happynewplaces.adapters.HappyPlaceAdapter
import com.example.happynewplaces.database.DataBaseHandler
import com.example.happynewplaces.databinding.ActivityMainBinding
import com.example.happynewplaces.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)

            startActivity(intent)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun setUpHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>){
        binding?.recyclerHappyNewPlacesList?.layoutManager = LinearLayoutManager(this)
        binding?.recyclerHappyNewPlacesList?.setHasFixedSize(true)

        val placesAdapter = HappyPlaceAdapter(this@MainActivity, happyPlaceList)

        binding?.recyclerHappyNewPlacesList?.adapter = placesAdapter
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DataBaseHandler(this)
        val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if(getHappyPlaceList.size > 0){
            for (i in getHappyPlaceList){
                binding?.recyclerHappyNewPlacesList?.visibility = View.VISIBLE
                binding?.txtNoRecordsAvailable?.visibility = View.GONE
                setUpHappyPlacesRecyclerView(getHappyPlaceList)
            }
        }else{
            binding?.recyclerHappyNewPlacesList?.visibility = View.GONE
            binding?.txtNoRecordsAvailable?.visibility = View.VISIBLE
        }

    }
}

//https://github.com/Karumi/Dexter