package com.example.smartnotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Locale

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val noteDao = db.noteDao()
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    // Gemini AI Model - Using gemini-1.5-flash which is standard now
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
                Analyze the sentiment and topics of this note: "$content"
                Provide exactly three parts separated by commas:
                1. A one-word Mood (e.g., Happy, Productive, Relaxed)
                2. Two relevant tags that describe the content (e.g., coding, grocery, health)
                
                Format your response exactly like this: Mood, Tag1, Tag2
                Example: Happy, exercise, sun
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val text = response.text?.trim() ?: ""
            
            Log.d("AI_DEBUG", "Raw response: $text")
            
            // Clean up the response in case AI adds markdown or extra punctuation
            val cleanedText = text.removeSurrounding("```").removePrefix("json").trim()
            val parts = cleanedText.split(",").map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
            
            val mood = parts.getOrNull(0)?.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            } ?: "Neutral"
            
            val tags = parts.drop(1).joinToString(", ").ifEmpty { "General" }
            
            Pair(mood, tags)
        } catch (e: Exception) {
            Log.e("AI_ERROR", "Analysis failed: ${e.message}", e)
            fallbackAnalysis(content)
        }
    }

    private fun fallbackAnalysis(content: String): Pair<String, String> {
        val lower = content.lowercase()
        val mood = when {
            lower.contains("super") || lower.contains("skvěle") || lower.contains("happy") -> "Happy"
            lower.contains("unaven") || lower.contains("smut") || lower.contains("sad") -> "Sad"
            else -> "Neutral"
        }
        return Pair(mood, "General")
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }
}
