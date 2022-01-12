package com.example.rickandmortyapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
data class Item(
    @PrimaryKey()
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String
)