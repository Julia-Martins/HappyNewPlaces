package com.example.happynewplaces.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happynewplaces.adapters.HappyPlaceAdapter
import com.example.happynewplaces.database.DataBaseHandler
import com.example.happynewplaces.databinding.ActivityMainBinding
import com.example.happynewplaces.models.HappyPlaceModel
import com.example.happynewplaces.utils.SwipeToDeleteCalBack
import com.example.happynewplaces.utils.SwipeToEditCalBack

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)

            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun setUpHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>){
        binding?.recyclerHappyNewPlacesList?.layoutManager = LinearLayoutManager(this)
        binding?.recyclerHappyNewPlacesList?.setHasFixedSize(true)

        val placesAdapter = HappyPlaceAdapter(this@MainActivity, happyPlaceList)

        binding?.recyclerHappyNewPlacesList?.adapter = placesAdapter

        placesAdapter.setOnClickListener(object: HappyPlaceAdapter.onClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity,
                    HappyPlaceDetailActivity::class.java)

                intent.putExtra(EXTRA_PLACE_DETAILS, model)

                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCalBack(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.recyclerHappyNewPlacesList?.adapter as HappyPlaceAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.absoluteAdapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)

        editItemTouchHelper.attachToRecyclerView(binding?.recyclerHappyNewPlacesList)

        val deleteSwipeHandler = object : SwipeToDeleteCalBack(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.recyclerHappyNewPlacesList?.adapter as HappyPlaceAdapter

                adapter.removeItem(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)

        deleteItemTouchHelper.attachToRecyclerView(binding?.recyclerHappyNewPlacesList)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity", "Cancelled or Back pressed")
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "Extra Place Details"
    }
}

//https://github.com/Karumi/Dexter