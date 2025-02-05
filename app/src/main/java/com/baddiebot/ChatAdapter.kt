package com.baddiebot

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_message_user
        } else {
            R.layout.item_message_bot
        }
        
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        private val avatarImage: CircleImageView = itemView.findViewById(R.id.avatarImage)
        private val messageCard: CardView = itemView.findViewById(R.id.messageCard)

        fun bind(message: ChatMessage) {
            messageText.text = message.content
            
            // Format timestamp to show actual time
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            timestampText.text = timeFormat.format(Date(message.timestamp))

            if (!message.isUser) {
                // Apply mood-specific styling for bot messages
                when (message.mood) {
                    BotMood.SASSY -> {
                        messageCard.setCardBackgroundColor(itemView.context.getColor(R.color.sassy_primary))
                        messageText.setTextColor(itemView.context.getColor(R.color.white))
                        avatarImage.setImageResource(R.drawable.avatar_sassy)
                    }
                    BotMood.DARK -> {
                        messageCard.setCardBackgroundColor(itemView.context.getColor(R.color.dark_primary))
                        messageText.setTextColor(itemView.context.getColor(R.color.dark_text))
                        avatarImage.setImageResource(R.drawable.avatar_dark)
                    }
                    BotMood.PLAYFUL -> {
                        messageCard.setCardBackgroundColor(itemView.context.getColor(R.color.playful_primary))
                        messageText.setTextColor(itemView.context.getColor(R.color.white))
                        avatarImage.setImageResource(R.drawable.avatar_playful)
                    }
                }
            } else {
                // User message styling
                messageCard.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                messageText.setTextColor(itemView.context.getColor(R.color.black))
                avatarImage.setImageResource(R.drawable.avatar_user)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
} 