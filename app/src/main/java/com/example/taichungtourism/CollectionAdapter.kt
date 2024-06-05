package com.example.taichungtourism

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.taichungtourism.databinding.FragmentCollectionListBinding
import com.squareup.picasso.Picasso

class CollectionAdapter(
    private val collectionList: List<Attraction>
) : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentCollectionListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = collectionList[position]
        holder.binding.apply {
            tvName.text = item.name
            tvAddress.text = item.address
            Picasso.get()
                .load(item.imageUrl)
                .into(imgAttraction)
        }
    }

    override fun getItemCount(): Int = collectionList.size

    inner class ViewHolder(val binding: FragmentCollectionListBinding) : RecyclerView.ViewHolder(binding.root)

}