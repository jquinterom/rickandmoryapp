package com.example.rickandmortyapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.rickandmortyapp.constants.Constants
import com.example.rickandmortyapp.data.Item
import com.example.rickandmortyapp.data.ItemRoomDatabase
import com.example.rickandmortyapp.databinding.FragmentHomeBinding
import com.example.rickandmortyapp.endPoints.EndPoint
import com.example.rickandmortyapp.http.HttpSingleton

class HomeFragment : Fragment() {

    // Instance database
    private val database: ItemRoomDatabase by lazy { ItemRoomDatabase.getDatabase(requireActivity().applicationContext) }

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    // to share the ViewModel across fragments.
    private val homeViewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(
            database.itemDao()
        )
    }


    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!



    private val RESULTS = "results"
    private val PAGE_START = 1 // pagina iniciar
    private var PAGE_CURRENT = 0 // pagina actual
    private var PAGE_SIZE = 10 // maxima cantidad de items
    private val PAGE_MODULE = 1 // para validar el modulo
    private var PAGE_CURRENT_MODULE = 0 // Modulo que puede cambiar



    private val layoutManager =  LinearLayoutManager(this.context)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Iniciar con 1
        getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get all data from characters
     * */
    private fun getData(){
            val url = EndPoint.CHARACTERS + PAGE_CURRENT.toString()

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    // Cargando la lista
                    val listItems = arrayListOf<Item>()
                    val results = response.getJSONArray(RESULTS)

                    results.let {
                        0.until(it.length()).map { i ->
                            it.optJSONObject(i)
                        }
                    }.map {
                        val item = Item(
                            it.getInt(Constants.ID),
                            it.getString(Constants.NAME),
                            it.getString(Constants.SPECIE),
                            it.getString(Constants.IMAGE)
                        )
                        listItems.add(item)
                    }

                    // Validar la carga de los 10 items *****

                    val adapter = ItemListAdapter {
                        addNewItem(it)
                    }


                    binding.rvItems.layoutManager = layoutManager
                    binding.rvItems.adapter = adapter
                    adapter.submitList(listItems)

                    setRecyclerViewScrollListener(layoutManager)
                },
                { error ->
                    // TODO: Handle error
                    Log.e("error", error.toString())
                }
            )

            // Access the RequestQueue through your singleton class.
            HttpSingleton.getInstance(binding.root.context).addToRequestQueue(jsonObjectRequest)


    }

    // ScrollListener for recyclerview
    private fun setRecyclerViewScrollListener(linearLayoutManager: LinearLayoutManager) {
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount

                val lastVisibleItemPosition: Int = linearLayoutManager.findLastVisibleItemPosition()
                val firstVisibleItemPosition: Int = linearLayoutManager.findFirstVisibleItemPosition()

                if (totalItemCount == lastVisibleItemPosition + 1) {
                    //getData()
                }
            }
        })
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(item: Item): Boolean {
        return homeViewModel.isEntryValid(
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
            homeViewModel.addNewItem(
                item.id,
                item.name,
                item.specie,
                item.image,
            )
        Toast.makeText(binding.root.context, "Registrado", Toast.LENGTH_LONG).show()
        }
    }
}