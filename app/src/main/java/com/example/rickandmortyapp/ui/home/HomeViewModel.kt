package com.example.rickandmortyapp.ui.home

import androidx.lifecycle.*
import com.example.rickandmortyapp.data.Item
import com.example.rickandmortyapp.data.ItemDao
import kotlinx.coroutines.launch

class HomeViewModel(private val itemDao: ItemDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    // Get all favorite items
    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(itemName: String, itemSpecie: String, itemImage: String): Boolean {
        if (itemName.isBlank() || itemSpecie.isBlank() || itemSpecie.isBlank()) {
            return false
        }
        return true
    }

    /**
     * Get a new [Item]
     * */
    private fun getNewItemEntry(id: Int, name: String, specie: String, image: String): Item{
        return Item(
            id = id,
            name = name,
            specie = specie,
            image = image
        )
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(id: Int, itemName: String, itemPrice: String, itemCount: String) {
        val newItem = getNewItemEntry(id, itemName, itemPrice, itemCount)
        insertItem(newItem)
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class HomeViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}