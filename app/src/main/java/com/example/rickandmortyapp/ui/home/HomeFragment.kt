package com.example.rickandmortyapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.rickandmortyapp.constants.Constants
import com.example.rickandmortyapp.data.Item
import com.example.rickandmortyapp.databinding.FragmentHomeBinding
import com.example.rickandmortyapp.endPoints.EndPoint
import com.example.rickandmortyapp.http.HttpSingleton

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val RESULTS = "results"
    private val PAGE_START = 1 // pagina iniciar
    private var PAGE_CURRENT = 0 // pagina actual
    private var PAGE_SIZE = 10 // maxima cantidad de items
    private val PAGE_MODULE = 1 // para validar el modulo
    private var PAGE_CURRENT_MODULE = 0 // Modulo que puede cambiar

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val layoutManager =  LinearLayoutManager(this.context)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

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

                    Log.d("PAGE", PAGE_CURRENT.toString())
                    Log.d("MODULE", PAGE_CURRENT_MODULE.toString())


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

                    // Validar la carga de los 10 items

                    val adapter = ItemListAdapter {
                        val action = HomeFragmentDirections.actionNavHomeToDetailItemFragment()
                        action.itemId = it.id
                        this.findNavController().navigate(action)
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
                    getData()
                }

                if(firstVisibleItemPosition == 0) {
                    if(PAGE_CURRENT > PAGE_START) PAGE_CURRENT-=1
                    getData()
                }
            }
        })
    }
}