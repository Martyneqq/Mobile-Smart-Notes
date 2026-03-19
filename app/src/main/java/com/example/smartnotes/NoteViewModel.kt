package com.example.smartnotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val noteDao = db.noteDao()
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    // Gemini AI Model - API Key is now safely hidden in BuildConfig
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val (mood, tags) = analyzeNote(content)
            val note = Note(
                title = title,
                content = content,
                mood = mood,
                tags = tags
            )
            noteDao.insertNote(note)
        }
    }

    private suspend fun analyzeNote(content: String): Pair<String, String> {
        return try {
            val prompt = """
                You are a smart note assistant. Analyze this note content: "$content"
                Return ONLY a simple comma-separated string in this format: MOOD, TAG1, TAG2
                Example: Happy, personal, ideas
                If you can't determine, use: Neutral, general
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val text = response.text?.trim() ?: ""
            
            val parts = text.split(",").map { it.trim() }
            val mood = parts.getOrNull(0) ?: "Neutral"
            val tags = parts.drop(1).joinToString(", ").ifEmpty { "General" }
            
            Pair(mood, tags)
        } catch (e: Exception) {
            Log.e("AI_ERROR", "Failed to analyze note", e)
            Pair("Neutral", "General")
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }
}
