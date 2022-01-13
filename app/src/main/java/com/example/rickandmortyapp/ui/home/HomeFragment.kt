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
import org.json.JSONArray
import java.lang.Exception

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
    private var PAGE_CURRENT = 1 // pagina actual
    private var PAGE_SIZE = 9 // maxima cantidad de items
    private var PAGE_CURRENT_MODULE = 1 // Modulo que puede cambiar
    private var PAGE_RESULT: JSONArray? = null
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

        // Iniciar con 0
        getData(PAGE_CURRENT - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get all data from characters
     * */
    private fun getData(page : Int){
        try {


            // Validamos el modulo
            if(PAGE_CURRENT_MODULE  == 1){
                // Cargamos por primera vez
            } else if(PAGE_CURRENT_MODULE == 2){
                // Cargamos la segunda parte
            } else if(PAGE_CURRENT_MODULE < 1){
                // Cargamos pagina anterior con modulo = 2
            } else if(PAGE_CURRENT_MODULE > 2){
                // Cargamos siguiente pagina con modulo = 1
            }


            val url = EndPoint.CHARACTERS + PAGE_CURRENT.toString()
            // Cargando la lista
            val listItems = arrayListOf<Item>()

            if (page != PAGE_CURRENT) {
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        PAGE_RESULT = response.getJSONArray(RESULTS)

                        if (PAGE_CURRENT_MODULE == 1) {
                            // Primeros 10 registros de la página
                            for (i in 0 until PAGE_SIZE) {
                                val it = PAGE_RESULT!!.getJSONObject(i)

                                val item = Item(
                                    it.getInt(Constants.ID),
                                    it.getString(Constants.NAME),
                                    it.getString(Constants.SPECIE),
                                    it.getString(Constants.IMAGE)
                                )
                                listItems.add(item)
                            }
                        } else {
                            // Ultimos 10 registros de la pagina
                            for (i in PAGE_SIZE until PAGE_RESULT!!.length() - 1) {
                                val it = PAGE_RESULT!!.getJSONObject(i)

                                val item = Item(
                                    it.getInt(Constants.ID),
                                    it.getString(Constants.NAME),
                                    it.getString(Constants.SPECIE),
                                    it.getString(Constants.IMAGE)
                                )
                                listItems.add(item)
                            }
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
            } else {
                if (PAGE_CURRENT_MODULE == 1) {
                    // Cargamos primera parte
                    for (i in 0 until PAGE_RESULT!!.length()) {
                        val it = PAGE_RESULT!!.getJSONObject(i)

                        val item = Item(
                            it.getInt(Constants.ID),
                            it.getString(Constants.NAME),
                            it.getString(Constants.SPECIE),
                            it.getString(Constants.IMAGE)
                        )
                        listItems.add(item)
                    }
                } else {
                    // Cargamos segunda parte
                    for (i in PAGE_SIZE..PAGE_RESULT!!.length()) {
                        val it = PAGE_RESULT!!.getJSONObject(i)

                        val item = Item(
                            it.getInt(Constants.ID),
                            it.getString(Constants.NAME),
                            it.getString(Constants.SPECIE),
                            it.getString(Constants.IMAGE)
                        )
                        listItems.add(item)
                    }
                }

                // Validar la carga de los 10 items *****

                val adapter = ItemListAdapter {
                    addNewItem(it)
                }

                binding.rvItems.layoutManager = layoutManager
                binding.rvItems.adapter = adapter
                adapter.submitList(listItems)

                setRecyclerViewScrollListener(layoutManager)
            }
        } catch (e: Exception){
            // Mostar mensaje amigable de no carga de datos
            e.printStackTrace()
        }
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
                    PAGE_CURRENT_MODULE +=1
                    getData()
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