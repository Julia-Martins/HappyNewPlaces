package com.example.happynewplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happynewplaces.R
import com.example.happynewplaces.activities.AddHappyPlaceActivity
import com.example.happynewplaces.activities.MainActivity
import com.example.happynewplaces.database.DataBaseHandler
import com.example.happynewplaces.databinding.ItemHappyPlaceBinding
import com.example.happynewplaces.models.HappyPlaceModel

open class HappyPlaceAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListenerVar: onClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_happy_place,
                parent,
                false
            )
        )
    }

    fun setOnClickListener(onClickListener: onClickListener){
        this.onClickListenerVar = onClickListener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding: ItemHappyPlaceBinding
        val model = list[position]

        if(holder is MyViewHolder){
            binding = ItemHappyPlaceBinding.bind(holder.itemView)

            binding.imgPlaceImageCircular.setImageURI(Uri.parse(model.image))
            binding.txtTitleCard.text = model.title
            binding.txtDescriptionCard.text = model.description

            holder.itemView.setOnClickListener {
                if(onClickListenerVar != null){
                    onClickListenerVar!!.onClick(position, model)
                }
            }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
        val intent = Intent(context, AddHappyPlaceActivity::class.java)

        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeItem(position: Int){
        val dbHandler = DataBaseHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(list[position])

        if (isDeleted > 0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface onClickListener{
        fun onClick(position: Int, model: HappyPlaceModel)
    }


    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}