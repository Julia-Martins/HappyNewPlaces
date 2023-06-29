package com.example.happynewplaces.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happynewplaces.R
import com.example.happynewplaces.models.HappyPlaceModel
import com.example.happynewplaces.databinding.ItemHappyPlaceBinding

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
    override fun getItemCount(): Int {
        return list.size
    }

    interface onClickListener{
        fun onClick(position: Int, model: HappyPlaceModel)
    }


    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}