package com.example.rickandmortyapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
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

    private val RESULTS = "results"
    private var PAGE_CURRENT = 0 // pagina actual
    private val PAGE_START = 1 // pagina actual
    private var PAGE_SIZE = 9 // maxima cantidad de items
    private var PAGE_CURRENT_MODULE = 1 // Modulo que puede cambiar
    private var PAGE_RESULT: JSONArray? = null
    private val layoutManager =  LinearLayoutManager(this.context)
    private var listItems = arrayListOf<Item>()

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
            PAGE_CURRENT_MODULE  == 1 -> {
                // Cargamos por primera vez
                return Constants.MODULE_ONE
            }
            PAGE_CURRENT_MODULE == 2 -> {
                // Cargamos la segunda parte
                return Constants.MODULE_TWO
            }
            PAGE_CURRENT_MODULE < 1 -> {
                if(PAGE_CURRENT != PAGE_START ) PAGE_CURRENT -=1
                PAGE_CURRENT_MODULE = 2
                return Constants.MODULE_THREE

                // Cargamos pagina anterior con modulo = 2
            }
            PAGE_CURRENT_MODULE > 2 -> {
                PAGE_CURRENT += 1
                PAGE_CURRENT_MODULE = 1
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
        try {
            // Validar el modulo y pagina a cargar
            val module = validateModule()
            if (PAGE_CURRENT != PAGE_START) { // Pagina inicial
                when (module) {
                    Constants.MODULE_ONE -> {
                        if(PAGE_CURRENT == 0){
                            // peticion a la API
                            getDataApi()
                            PAGE_CURRENT += PAGE_START
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
        } catch (e: Exception){
            // Mostar mensaje amigable de no carga de datos
            e.printStackTrace()
        }
    }

    /**
     * Realizar peticion a la API
     * */
    private fun getDataApi(){
        var url = EndPoint.CHARACTERS + PAGE_CURRENT.toString()
        if(PAGE_CURRENT == 0){ // Cargar pagina 1
            url = EndPoint.CHARACTERS + PAGE_START.toString()
        }

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
     * Obtener la información de la variable local
     * */
    private fun getDataLocal(){
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
}