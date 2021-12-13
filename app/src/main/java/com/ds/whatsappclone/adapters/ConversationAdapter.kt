package com.ds.whatsappclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ds.whatsappclone.R
import com.ds.whatsappclone.util.Message

class ConversationAdapter(private var messages:ArrayList<Message>, val userId:String?) : RecyclerView.Adapter<ConversationAdapter.MessagesViewHolder>() {

    companion object{
        val MESSAGE_CURRENT_USER = 1
        val MESSAGE_OTHER_USER = 2
    }

    fun addMessage(message: Message){
        messages.add(message)
        notifyDataSetChanged()
    }

    class MessagesViewHolder(val view: View): RecyclerView.ViewHolder(view){
        fun bind(message: Message){
            view.findViewById<TextView>(R.id.tv_messasge).text = message.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        if(viewType == MESSAGE_CURRENT_USER){
            return MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_current_user_message, parent, false))
        }else{
            return MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_other_user_message, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(messages[position].sentBy.equals(userId)){
            return MESSAGE_CURRENT_USER
        }else{
            return MESSAGE_OTHER_USER
        }
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }

}