package com.example.rickandmortyapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var pageCurrent = 0 // pagina actual
    private val pageStart = 1 // pagina actual
    private var pageSize = 9 // maxima cantidad de items
    private var pageCurrentModule = 1 // Modulo que puede cambiar
    private var pageResult: JSONArray? = null
    private val layoutManager =  LinearLayoutManager(this.context)
    private var listItems = arrayListOf<Item>()
    private var listItemsFavorites = mutableListOf<Item>()


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
     * Validar el modulo y pagina a cargar
     * */
    private fun validateModule(): Int{
        // Validamos el modulo
        when {
            pageCurrentModule  == 1 -> {
                // Cargamos por primera vez
                return Constants.MODULE_ONE
            }
            pageCurrentModule == 2 -> {
                // Cargamos la segunda parte
                return Constants.MODULE_TWO
            }
            pageCurrentModule < 1 -> {
                if(pageCurrent != pageStart ) pageCurrent -=1
                pageCurrentModule = 2
                return Constants.MODULE_THREE

                // Cargamos pagina anterior con modulo = 2
            }
            pageCurrentModule > 2 -> {
                pageCurrent += 1
                pageCurrentModule = 1
                return Constants.MODULE_FOUR

                // Cargamos siguiente pagina con modulo = 1
            }
            else -> {
                return  -1
            }
        }
    }

    /**
     * Get all data from characters
     * */
    private fun getData(){
        binding.pBar.visibility = View.VISIBLE
        try {
            // Validar el modulo y pagina a cargar
            val module = validateModule()
            if (pageCurrent != pageStart) { // Pagina inicial
                when (module) {
                    Constants.MODULE_ONE -> {
                        if(pageCurrent == 0){
                            // peticion a la API
                            getDataApi()
                            pageCurrent += pageStart
                        } else {
                            // Carga de lista ya existente
                            getDataLocal()
                        }
                    }
                    Constants.MODULE_TWO -> {
                        // Carga de lista ya existente
                        getDataLocal()
                    }
                    Constants.MODULE_THREE, Constants.MODULE_FOUR -> {
                        // peticion a la api y se carga la segunda parte
                        getDataApi()
                    }
                }
            }
            binding.pBar.visibility = View.GONE
        } catch (e: Exception){
            // Mostar mensaje amigable de no carga de datos
            e.printStackTrace()
            binding.pBar.visibility = View.GONE
        }
    }

    /**
     * Realizar peticion a la API
     * */
    private fun getDataApi(){
        var url = EndPoint.CHARACTERS + pageCurrent.toString()
        if(pageCurrent == 0){ // Cargar pagina 1
            url = EndPoint.CHARACTERS + pageStart.toString()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                pageResult = response.getJSONArray(results)

                if (pageCurrentModule == 1) {
                    // Primeros 10 registros de la página
                    for (i in 0 until pageSize) {
                        val it = pageResult!!.getJSONObject(i)

                        val item = Item(
                            it.getInt(Constants.ID),
                            it.getString(Constants.NAME),
                            it.getString(Constants.SPECIE),
                            it.getString(Constants.IMAGE)
                        )
                        listItemsFavorites.map {
                            if(it.id == item.id){
                                item.favorite = Constants.ITEM_FAVORITE
                            }
                        }

                        listItems.add(item)
                    }
                } else {
                    // Ultimos 10 registros de la pagina
                    for (i in pageSize until pageResult!!.length() - 1) {
                        val it = pageResult!!.getJSONObject(i)

                        val item = Item(
                            it.getInt(Constants.ID),
                            it.getString(Constants.NAME),
                            it.getString(Constants.SPECIE),
                            it.getString(Constants.IMAGE)
                        )
                        listItemsFavorites.map {
                            if(it.id == item.id){
                                item.favorite = Constants.ITEM_FAVORITE
                            }
                        }

                        listItems.add(item)
                    }
                }

                // Validar la carga de los 10 items *****
                val adapter = ItemListAdapter {
                    registerItem(it)
                }

                binding.rvItems.layoutManager = layoutManager
                binding.rvItems.adapter = adapter
                adapter.submitList(listItems)
            },
            { error ->
                // TODO: Handle error
                Log.e("error", error.toString())
            }
        )
        // Access the RequestQueue through your singleton class.
        HttpSingleton.getInstance(binding.root.context).addToRequestQueue(jsonObjectRequest)
    }

    /**
     * Obtener información con el buscador
     * */
    private fun getDataApiSearch(parameter: String){
        binding.pBar.visibility = View.VISIBLE
        binding.textHome.visibility = View.GONE

        binding.rvItems.adapter = null

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
                            if(it.id == item.id){
                                item.favorite = Constants.ITEM_FAVORITE
                            }
                        }
                        listItems.add(item)
                    }

                    val adapter = ItemListAdapter {
                        registerItem(it)
                    }

                    binding.rvItems.layoutManager = layoutManager
                    binding.rvItems.adapter = adapter
                    adapter.submitList(listItems)

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
        }  catch (e: Exception){
            binding.textHome.visibility = View.VISIBLE
            e.printStackTrace()
        }
    }

    /**
     * Obtener la información de la variable local
     * */
    private fun getDataLocal(){
        if (pageCurrentModule == 1) {
            // Primeros 10 registros de la página
            for (i in 0 until pageSize) {
                val it = pageResult!!.getJSONObject(i)

                val item = shareViewModel.getNewItemEntry(it.getInt(Constants.ID),
                    it.getString(Constants.NAME),
                    it.getString(Constants.SPECIE),
                    it.getString(Constants.IMAGE)
                )
                listItemsFavorites.map {
                    if(it.id == item.id){
                        item.favorite = Constants.ITEM_FAVORITE
                    }
                }
                listItems.add(item)
            }
        } else {
            // Ultimos 10 registros de la pagina
            for (i in pageSize until pageResult!!.length() - 1) {
                val it = pageResult!!.getJSONObject(i)

                val item = shareViewModel.getNewItemEntry(it.getInt(Constants.ID),
                    it.getString(Constants.NAME),
                    it.getString(Constants.SPECIE),
                    it.getString(Constants.IMAGE)
                )

                listItemsFavorites.map {
                    if(it.id == item.id){
                        item.favorite = Constants.ITEM_FAVORITE
                    }
                }
                listItems.add(item)
            }
        }

        val adapter = ItemListAdapter {
            addNewItem(it)
        }

        binding.rvItems.layoutManager = layoutManager
        binding.rvItems.adapter = adapter
        adapter.submitList(listItems)
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
            shareViewModel.addNewItem(
                item.id,
                item.name,
                item.specie,
                item.image,
                Constants.ITEM_FAVORITE
            )
        Toast.makeText(binding.root.context, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Validate [Item] count and register
     */
    private fun registerItem(item: Item){
        shareViewModel.allItems.observe(this.viewLifecycleOwner){
            items ->
            if(items.size < Constants.MAX_FAVORITES){
                addNewItem(item)
            } else {
                Toast.makeText(binding.root.context, "Limite de favoritos alcanzado", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val search = menu.findItem(R.id.search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search"

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
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
            listItems.clear()
            getDataApi()
            false
        }
    }
}