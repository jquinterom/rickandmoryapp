package com.example.rickandmortyapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        val url = EndPoint.CHARACTERS

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Cargando la lista
                val listItems = arrayListOf<Item>()

                val results = response.getJSONArray(RESULTS)

                results.let {
                    0.until(it.length()).map {
                        i -> it.optJSONObject(i)
                    }
                }.map {
                    val item = Item(it.getInt(Constants.ID), it.getString(Constants.NAME), it.getString(Constants.SPECIE), it.getString(Constants.IMAGE))
                    listItems.add(item)
                }

                val adapter = ItemListAdapter{
                    val action = HomeFragmentDirections.actionNavHomeToDetailItemFragment()
                    action.itemId = it.id
                    this.findNavController().navigate(action)
                }

                binding.rvItems.layoutManager = LinearLayoutManager(this.context)
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
}