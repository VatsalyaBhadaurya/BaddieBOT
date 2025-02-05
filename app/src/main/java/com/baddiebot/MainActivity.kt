package com.baddiebot

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
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
            val message = messageInput.text?.toString()?.trim() ?: ""
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                messageInput.text?.clear()
            }
        }

        moodToggleButton.setOnClickListener { view ->
            showMoodPopupMenu(view)
        }

        // Add click listener to logo
        findViewById<ImageView>(R.id.appLogo).setOnClickListener {
            showAboutDialog()
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

    private fun showAboutDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_about)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // LinkedIn button
        dialog.findViewById<MaterialButton>(R.id.linkedinButton).setOnClickListener {
            openUrl("https://linkedin.com/in/vatsalya-bhadaurya")
        }

        // GitHub button
        dialog.findViewById<MaterialButton>(R.id.githubButton).setOnClickListener {
            openUrl("https://github.com/VatsalyaBhadaurya/BaddieBOT")
        }

        // Email button
        dialog.findViewById<MaterialButton>(R.id.emailButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:vatbhadaurya@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Regarding BaddieBOT")
            }
            startActivity(Intent.createChooser(intent, "Send email"))
        }

        dialog.show()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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