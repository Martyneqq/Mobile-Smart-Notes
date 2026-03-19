package com.example.smartnotes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val tags: String, // Stored as comma-separated string for simplicity
    val mood: String,
    val timestamp: Long = System.currentTimeMillis()
)
