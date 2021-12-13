package com.ds.whatsappclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ds.whatsappclone.R
import com.ds.whatsappclone.listeners.ChatsClickListener
import com.ds.whatsappclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsAdapter(val chats: ArrayList<String>) : RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var clickListener:ChatsClickListener? = null;

    class ChatsViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val firebaseDB = FirebaseFirestore.getInstance()
        private val userId = FirebaseAuth.getInstance().currentUser?.uid

        private var layout = view.findViewById<RelativeLayout>(R.id.chatLayout)
        private var chatIV = view.findViewById<ImageView>(R.id.iv_chat)
        private var chatNameTV = view.findViewById<TextView>(R.id.tv_chat)
        private var progressLayout = view.findViewById<LinearLayout>(R.id.progessLayout)
        private var partnerId:String? = null
        private var chatImageUrl:String? = null
        private var chatName:String? = null

        fun bind(chatId: String, listener : ChatsClickListener?){
//            populateImage(chatIV.context, "", chatIV, R.drawable.default_user)
//            chatName.text = chatId
            progressLayout.visibility = View.VISIBLE
            progressLayout.setOnTouchListener { v, event ->  true}

            firebaseDB.collection(DATA_CHATS)
                .document(chatId)
                .get()
                .addOnSuccessListener {
                    val chatParticipants = it[DATA_CHATS_PARTICIPANTS]
                    if(chatParticipants != null){
                        for (participant in chatParticipants as ArrayList<String>){
                            if(participant != null && !participant.equals(userId)){
                                partnerId = participant
                                firebaseDB.collection(DATA_USERS)
                                    .document(partnerId!!)
                                    .get()
                                    .addOnSuccessListener {
                                        val user = it.toObject(User::class.java)
                                        chatImageUrl = user?.imageUrl
                                        chatName = user?.name
                                        chatNameTV.text = user?.name
                                        populateImage(chatIV.context, user?.imageUrl, chatIV, R.drawable.default_user)
                                        progressLayout.visibility = View.GONE
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                        progressLayout.visibility = View.GONE
                                    }
                                layout.setOnClickListener { listener?.onChatClicked(chatId, partnerId, chatImageUrl, chatName) }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    progressLayout.visibility = View.GONE
                    e.printStackTrace()
                }
        }
    }

    fun updateChats(updateChats:ArrayList<String>){
        chats.clear()
        chats.addAll(updateChats)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: ChatsClickListener){
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChatsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        )

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(chats[position], clickListener)
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}