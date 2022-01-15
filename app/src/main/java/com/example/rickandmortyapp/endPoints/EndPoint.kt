package com.example.rickandmortyapp.endPoints

object EndPoint {
    private const val API = "https://rickandmortyapi.com/api/character/"
    const val CHARACTERS = "$API?page="
    const val CHARACTERS_FILTER_NAME = "$API?name="
}