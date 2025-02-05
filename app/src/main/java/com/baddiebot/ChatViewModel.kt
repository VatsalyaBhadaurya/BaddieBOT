package com.baddiebot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatViewModel : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages
    
    private val _currentMood = MutableLiveData<BotMood>(BotMood.SASSY)
    val currentMood: LiveData<BotMood> = _currentMood
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val HUGGING_FACE_API_TOKEN = "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"
    private val API_URL = "https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill"

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val currentList = _chatMessages.value?.toMutableList() ?: mutableListOf()
            currentList.add(ChatMessage(message, isUser = true))
            _chatMessages.value = currentList

            try {
                val currentMood = _currentMood.value ?: BotMood.SASSY
                val response = withContext(Dispatchers.IO) {
                    getBotResponse(message, currentMood)
                }
                
                val updatedList = _chatMessages.value?.toMutableList() ?: mutableListOf()
                updatedList.add(ChatMessage(response, isUser = false, mood = currentMood))
                _chatMessages.value = updatedList
            } catch (e: Exception) {
                val currentMood = _currentMood.value ?: BotMood.SASSY
                val updatedList = _chatMessages.value?.toMutableList() ?: mutableListOf()
                updatedList.add(ChatMessage(getOfflineResponse(message, currentMood), isUser = false, mood = currentMood))
                _chatMessages.value = updatedList
            }
        }
    }

    private suspend fun getBotResponse(message: String, mood: BotMood): String {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("inputs", message)
                    put("parameters", JSONObject().apply {
                        put("max_length", 150)
                        put("min_length", 20)
                        put("do_sample", true)
                        put("top_k", 40)
                        put("top_p", 0.9)
                        put("temperature", when(mood) {
                            BotMood.SASSY -> 1.0  // Extra sassy and bold
                            BotMood.DARK -> 0.8   // Mysterious and edgy
                            BotMood.PLAYFUL -> 0.9 // Wild and chaotic
                        })
                    })
                }

                val request = Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer $HUGGING_FACE_API_TOKEN")
                    .addHeader("Content-Type", "application/json")
                    .post(
                        jsonBody.toString()
                            .toRequestBody("application/json".toMediaType())
                    )
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: throw IOException("Empty response")
                
                println("API Response: $responseBody")
                
                if (!response.isSuccessful) {
                    println("API Error: ${response.code} - ${response.message}")
                    throw IOException("API request failed: ${response.code}")
                }

                try {
                    // Parse as JSONArray first
                    val jsonArray = org.json.JSONArray(responseBody)
                    val firstResponse = jsonArray.getJSONObject(0)
                    val generatedText = firstResponse.getString("generated_text")
                        .replace("\"", "")
                        .trim()
                    
                    println("Generated Text: $generatedText")
                    applyMoodStyle(generatedText, mood)
                } catch (e: Exception) {
                    println("Parsing Error: ${e.message}")
                    e.printStackTrace()
                    getOfflineResponse(message, mood)
                }
            } catch (e: Exception) {
                println("Network Error: ${e.message}")
                e.printStackTrace()
                getOfflineResponse(message, mood)
            }
        }
    }

    private fun applyMoodStyle(response: String, mood: BotMood): String {
        return when(mood) {
            BotMood.SASSY -> {
                "Girl, listen... ${response.trim()} 💅"
                    .replace(".", "!")
                    .replace("(?i)hello".toRegex(), "Um, hi sweetie")
                    .replace("(?i)yes".toRegex(), "periodt!")
                    .replace("(?i)no".toRegex(), "absolutely not")
                    .plus(" *hair flip* ✨")
            }
            BotMood.DARK -> {
                "*emerges from shadows* ${response.trim()} 🖤"
                    .replace("(?i)happy".toRegex(), "darkly amused")
                    .replace("(?i)hello".toRegex(), "we meet again")
                    .replace("(?i)good".toRegex(), "intriguing")
                    .plus(" *vanishes mysteriously* 🌑")
            }
            BotMood.PLAYFUL -> {
                "OMFG! ${response.trim()} 🎉"
                    .replace(".", "!!!")
                    .replace("(?i)hello".toRegex(), "HEYYYY")
                    .replace("(?i)good".toRegex(), "amazing")
                    .replace("(?i)yes".toRegex(), "YASSSS")
                    .plus(" *twerks* 💃")
            }
        }
    }

    private fun getOfflineResponse(message: String, mood: BotMood): String {
        val responses = when(mood) {
            BotMood.SASSY -> listOf(
                "Ugh, the wifi is giving me attitude rn! Can't even! 💅",
                "Sorry bestie, too busy being fabulous to connect! ✨",
                "Internet's being basic, gimme a sec! 👑",
                "Not me having connection issues... the audacity! 💁‍♀️"
            )
            BotMood.DARK -> listOf(
                "*connection dissolves into the void* ... 🖤",
                "The digital darkness consumes all... ⛓️",
                "Your message echoes in the endless abyss... 💀",
                "The shadows interfere with our connection... 🌑"
            )
            BotMood.PLAYFUL -> listOf(
                "OMG bestie! Connection's being so extra rn! 🎉",
                "Spilled tea on the servers, BRB! ☕",
                "This connection is giving main character energy... but make it chaos! 💫",
                "Not me ghosting you because of bad internet! 👻"
            )
        }
        return responses.random()
    }

    fun setMood(mood: BotMood) {
        _currentMood.value = mood
    }
} 