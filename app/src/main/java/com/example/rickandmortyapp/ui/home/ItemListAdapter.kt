package com.example.rickandmortyapp.ui.home

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.ImageRequest
import com.example.rickandmortyapp.data.Item
import com.example.rickandmortyapp.databinding.ItemListHomeBinding
import com.example.rickandmortyapp.http.HttpSingleton
import java.lang.Exception

class ItemListAdapter(private val onItemClicked: (Item) -> Unit) :
    ListAdapter<Item, ItemListAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder(
            ItemListHomeBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onItemClicked)
    }

    class ItemViewHolder(private var binding: ItemListHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, onItemClicked: (Item) -> Unit) {
            try {
                binding.txtName.text = item.name
                binding.txtSpecie.text = item.specie

                // Evento para agregar a favoritos
                binding.btnFavorite.setOnClickListener {
                    onItemClicked(item)
                }

                // Cargando imagen
                val imageRequest = ImageRequest(
                    item.image,
                    { bitmap ->
                        binding.imgItem.setImageBitmap(bitmap)
                    },
                    0,
                    0,
                    ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                    { error ->
                        error.printStackTrace()
                    }
                )
                HttpSingleton.getInstance(binding.root.context).addToRequestQueue(imageRequest)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }
}