package com.example.rickandmortyapp.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.rickandmortyapp.data.ItemRoomDatabase
import com.example.rickandmortyapp.databinding.FragmentFavoritesBinding
import com.example.rickandmortyapp.ui.HomeViewModelFactory
import com.example.rickandmortyapp.ui.ShareViewModel

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get data favorites
     * */
    private fun getData(){

    }
}