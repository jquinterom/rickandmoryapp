package com.example.rickandmortyapp.ui.favorites

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rickandmortyapp.R
import com.example.rickandmortyapp.data.Item
import com.example.rickandmortyapp.data.ItemRoomDatabase
import com.example.rickandmortyapp.databinding.FragmentFavoritesBinding
import com.example.rickandmortyapp.ui.HomeViewModelFactory
import com.example.rickandmortyapp.ui.ShareViewModel
import com.example.rickandmortyapp.ui.ItemListAdapter
import com.example.rickandmortyapp.ui.home.HomeFragmentDirections
import java.util.*

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


    // Difinir variable para agregar a favoritos
    private val adapter = ItemListAdapter {
        removeItem(it)
    }


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
    private fun getData() {
        binding.rvItems.layoutManager = LinearLayoutManager(this.context)
        binding.rvItems.adapter = adapter

        shareViewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
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
    private fun removeItem(item: Item) {
        shareViewModel.deleteItem(item)
    }

    // region menu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(FavoritesFragmentDirections.actionNavGalleryToNavHome())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val search = menu.findItem(R.id.search)
        menu.findItem(R.id.count).isVisible = false
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search favorite"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Obtener datos del API
                val list = mutableListOf<Item>()

                shareViewModel.allItems.observe(viewLifecycleOwner) { items ->
                    if (items.isNotEmpty()) {
                        binding.textGallery.visibility = View.GONE
                    } else {
                        binding.textGallery.visibility = View.VISIBLE
                    }

                    items.map {
                        if (it.name.lowercase(Locale.getDefault())
                                .contains(query.toString().lowercase(Locale.getDefault()))
                        ) {
                            list.add(it)
                        }
                    }
                    adapter.submitList(list)
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        searchView.setOnCloseListener {
            //listItems.clear()
            getData()
            false
        }
    }
    // endregion
}