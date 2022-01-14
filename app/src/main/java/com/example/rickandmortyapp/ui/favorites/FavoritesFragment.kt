package com.example.rickandmortyapp.ui.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rickandmortyapp.data.Item
import com.example.rickandmortyapp.data.ItemRoomDatabase
import com.example.rickandmortyapp.databinding.FragmentFavoritesBinding
import com.example.rickandmortyapp.ui.HomeViewModelFactory
import com.example.rickandmortyapp.ui.ShareViewModel
import com.example.rickandmortyapp.ui.home.ItemListAdapter

class FavoritesFragment : Fragment() {

    // Instance database
    private val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(requireActivity().applicationContext) }

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    // to share the ViewModel across fragments.
    private val shareViewModel: ShareViewModel by activityViewModels {
        HomeViewModelFactory(
            database.itemDao()
        )
    }

    private var _binding: FragmentFavoritesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        shareViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener favoritos local (ROOM)
        getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get data favorites
     * */
    private fun getData(){
        // Difinir variable para agregar a favoritos
        val adapter = ItemListAdapter {
            removeItem(it)
        }
        binding.rvItems.layoutManager = LinearLayoutManager(this.context)
        binding.rvItems.adapter = adapter

        shareViewModel.allItems.observe(this.viewLifecycleOwner){
            items ->
            if (items.isNotEmpty()){
                binding.textGallery.visibility = View.GONE
            } else {
                binding.textGallery.visibility = View.VISIBLE
            }
            items.let {
                adapter.submitList(it)
            }
        }
    }

    /**
     * Remove item fom favorites
     * */
    private fun removeItem(item: Item){
        shareViewModel.deleteItem(item)
    }
}