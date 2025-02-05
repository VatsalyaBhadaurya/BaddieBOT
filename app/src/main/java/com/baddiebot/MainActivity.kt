package com.baddiebot

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var moodToggleButton: Button
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        moodToggleButton = findViewById(R.id.moodToggleButton)

        chatAdapter = ChatAdapter()
        chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                messageInput.text.clear()
            }
        }

        moodToggleButton.setOnClickListener { view ->
            showMoodPopupMenu(view)
        }
    }

    private fun showMoodPopupMenu(view: View) {
        PopupMenu(this, view).apply {
            menu.add("💅 Queen Behavior 👑").setOnMenuItemClickListener {
                viewModel.setMood(BotMood.SASSY)
                moodToggleButton.text = "Mood: Sassy AF 💅"
                true
            }
            menu.add("🖤 Villain Era 💀").setOnMenuItemClickListener {
                viewModel.setMood(BotMood.DARK)
                moodToggleButton.text = "Mood: Dark AF 🖤"
                true
            }
            menu.add("✨ Chaotic Energy 🎉").setOnMenuItemClickListener {
                viewModel.setMood(BotMood.PLAYFUL)
                moodToggleButton.text = "Mood: Wild AF 🎉"
                true
            }
            show()
        }
    }

    private fun setupObservers() {
        viewModel.chatMessages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            chatRecyclerView.scrollToPosition(messages.size - 1)
        }

        viewModel.currentMood.observe(this) { mood ->
            val moodText = when (mood) {
                BotMood.SASSY -> "Mood: Sassy AF 💅"
                BotMood.DARK -> "Mood: Dark AF 🖤"
                BotMood.PLAYFUL -> "Mood: Wild AF 🎉"
            }
            moodToggleButton.text = moodText
        }
    }
} 