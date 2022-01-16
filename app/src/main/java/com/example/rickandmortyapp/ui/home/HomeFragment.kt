package com.example.rickandmortyapp.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import kotlinx.coroutines.flow.Flow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.rickandmortyapp.R
import com.example.rickandmortyapp.constants.Constants
import com.example.rickandmortyapp.data.Item
import com.example.rickandmortyapp.data.ItemRoomDatabase
import com.example.rickandmortyapp.databinding.FragmentHomeBinding
import com.example.rickandmortyapp.endPoints.EndPoint
import com.example.rickandmortyapp.http.HttpSingleton
import com.example.rickandmortyapp.ui.HomeViewModelFactory
import com.example.rickandmortyapp.ui.ShareViewModel
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import java.lang.Exception

class HomeFragment : Fragment() {

    // Instance database
    private val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(requireActivity().applicationContext) }

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    // to share the ViewModel across fragments.
    private val shareViewModel: ShareViewModel by activityViewModels {
        HomeViewModelFactory(
            database.itemDao()
        )
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val results = "results"
    private var pageCurrent = 1 // pagina actual
    private val pageStart = 1 // pagina actual
    private var pageSize = 9 // maxima cantidad de items
    private var pageResult: JSONArray? = null
    private val layoutManager = LinearLayoutManager(this.context)
    private var listItems = arrayListOf<Item>()
    private var listItemsFavorites = mutableListOf<Item>()

    private val adapter = ItemListAdapter {
        registerItem(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        shareViewModel.text.observe(viewLifecycleOwner, {
            binding.textHome.text = it
        })

        shareViewModel.allItems.observe(this.viewLifecycleOwner) {
            listItemsFavorites = it as MutableList<Item>

        }

        binding.rvItems.layoutManager = layoutManager
        binding.rvItems.adapter = adapter
        adapter.submitList(listItems)

        pagination()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Iniciar con 0
        getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get all data from characters
     * */
    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        binding.pBar.visibility = View.VISIBLE
        try {
            val url = EndPoint.CHARACTERS + pageCurrent.toString()

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    pageResult = response.getJSONArray(results)

                    // Primeros 10 registros de la página
                    for (i in 0 until pageResult!!.length() -1) {
                        val it = pageResult!!.getJSONObject(i)

                        val item = Item(
                            it.getInt(Constants.ID),
                            it.getString(Constants.NAME),
                            it.getString(Constants.SPECIE),
                            it.getString(Constants.IMAGE)
                        )
                        listItemsFavorites.map {
                            if (it.id == item.id) {
                                item.favorite = Constants.ITEM_FAVORITE
                            }
                        }
                        listItems.add(item)
                    }

                    adapter.notifyDataSetChanged()
                },
                { error ->
                    // TODO: Handle error
                    Log.e("error", error.toString())
                }
            )
            // Access the RequestQueue through your singleton class.
            HttpSingleton.getInstance(binding.root.context).addToRequestQueue(jsonObjectRequest)
            binding.pBar.visibility = View.GONE
        } catch (e: Exception) {
            // Mostar mensaje amigable de no carga de datos
            e.printStackTrace()
            binding.pBar.visibility = View.GONE
        }
    }

    /**
     * Obtener información con el buscador
     * */
    @SuppressLint("NotifyDataSetChanged")
    private fun getDataApiSearch(parameter: String) {
        binding.pBar.visibility = View.VISIBLE
        binding.textHome.visibility = View.GONE
        listItems.clear()

        val url = EndPoint.CHARACTERS_FILTER_NAME + parameter

        try {
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    listItems.clear()

                    pageResult = response.getJSONArray(results)
                    val pageSize: Int = if (pageResult!!.length() < pageSize) {
                        pageResult!!.length()
                    } else {
                        pageSize
                    }

                    // Solo los 10 primeros registros de la página
                    for (i in 0 until pageSize) {
                        val it = pageResult!!.getJSONObject(i)

                        val item = Item(
                            it.getInt(Constants.ID),
                            it.getString(Constants.NAME),
                            it.getString(Constants.SPECIE),
                            it.getString(Constants.IMAGE)
                        )

                        listItemsFavorites.map {
                            if (it.id == item.id) {
                                item.favorite = Constants.ITEM_FAVORITE
                            }
                        }
                        listItems.add(item)
                    }

                    adapter.notifyDataSetChanged()

                    binding.pBar.visibility = View.GONE
                },
                { error ->
                    if (error.networkResponse.statusCode == Constants.NotFound) {
                        binding.textHome.visibility = View.VISIBLE
                    } else {
                        binding.textHome.visibility = View.GONE
                        error.printStackTrace()
                    }

                    binding.pBar.visibility = View.GONE
                }
            )

            // Access the RequestQueue through your singleton class.
            HttpSingleton.getInstance(binding.root.context).addToRequestQueue(jsonObjectRequest)
        } catch (e: Exception) {
            binding.textHome.visibility = View.VISIBLE
            e.printStackTrace()
        }
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(item: Item): Boolean {
        return shareViewModel.isEntryValid(
            item.name,
            item.specie,
            item.image,
        )
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem(item: Item) {
        isEntryValid(item)
        if (isEntryValid(item)) {
            var add = true
            listItemsFavorites.map {
                if(it.id == item.id){
                    add = false
                }
            }

            val message : String = if(add){
                // Agregar
                shareViewModel.addNewItem(
                    item.id,
                    item.name,
                    item.specie,
                    item.image,
                    Constants.ITEM_FAVORITE
                )
                "Agregado a favoritos"
            } else {
                // Eliminar
                shareViewModel.deleteItem(item)
                "Eliminado de favoritos"
            }
            view?.let {
                Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                    .setAction(Constants.FAVORITES, null).show()
            }
        }
    }

    /**
     * Validate [Item] count and register
     */
    private fun registerItem(item: Item) {
        if(listItemsFavorites.size < Constants.MAX_FAVORITES){
            addNewItem(item)
        } else {
            view?.let {
                Snackbar.make(it, "Limite de favoritos alcanzado", Snackbar.LENGTH_LONG)
                    .setAction(Constants.FAVORITES, null).show()
            }
        }
    }

    // region menu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val search = menu.findItem(R.id.search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Obtener datos del API
                getDataApiSearch(query.toString())
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        searchView.setOnCloseListener {
            pageCurrent = pageStart

            listItems.clear()
            binding.textHome.visibility = View.GONE
            getData()
            false
        }
    }
    // endregion

    /**
     * Pagination
     */
    private fun pagination() {
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var load: Boolean = true
            // Variables paginacion
            private var totalItemsCount = 0
            private var firstVisibleItem = 0
            private var visibleItemCount = 0
            private var previousTotal = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(dy > 0){
                    firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    totalItemsCount = layoutManager.itemCount
                    visibleItemCount = layoutManager.findFirstVisibleItemPosition()
                    if(load){
                        if(totalItemsCount > previousTotal){
                            previousTotal = totalItemsCount;
                            pageCurrent+=1
                            load = false;
                        }
                    }

                    if (!load && (firstVisibleItem + visibleItemCount) >= totalItemsCount ) {
                        getData()
                        load = true
                        Log.d("loading", "Loading")
                    }
                }
            }
        })
    }
}