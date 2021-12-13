package com.ds.whatsappclone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ds.whatsappclone.R
import com.ds.whatsappclone.adapters.ConversationAdapter
import com.ds.whatsappclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId  = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private var chatId:String? = null
    private var imageUrl:String? = null
    private var otherUserId:String? = null
    private var chatName:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        chatId = intent.extras?.getString(PARAM_CHAT_ID)
        imageUrl = intent.extras?.getString(PARAM_IMAGE_URL)
        otherUserId = intent.extras?.getString(PARAM_OTHER_USER_ID)
        chatName = intent.extras?.getString(PARAM_CHAT_NAME)
        if(chatId.isNullOrEmpty() || userId.isNullOrEmpty()){
            Toast.makeText(this, "Chat room error", Toast.LENGTH_SHORT).show()
            finish()
        }

        tp_topName.text = chatName
        populateImage(this, imageUrl, iv_topPhoto, R.drawable.default_user)

        rv_messages.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }
        firebaseDb.collection(DATA_CHATS)
            .document(chatId!!)
            .collection(DATA_CHAT_MESSAGES)
            .orderBy(DATA_CHAT_MESSAGE_TIME)
            .addSnapshotListener { value, error ->
                if(error != null){
                    error.printStackTrace()
                    return@addSnapshotListener
                }else{
                    if(value != null){
                        for(change in value.documentChanges){
                            when(change.type){
                                DocumentChange.Type.ADDED -> {
                                    val message = change.document.toObject(Message::class.java)
                                    if(message != null){
                                        conversationAdapter.addMessage(message)
                                        rv_messages.post {
                                            rv_messages.smoothScrollToPosition(conversationAdapter.itemCount - 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    fun onSend(view: View){
        if(!ed_message.text.isNullOrEmpty()){
            val message = Message(userId, ed_message.text.toString(), System.currentTimeMillis())
            firebaseDb.collection(DATA_CHATS)
                .document(chatId!!)
                .collection(DATA_CHAT_MESSAGES)
                .document()
                .set(message)
            ed_message.setText("", TextView.BufferType.EDITABLE)
        }
    }

    companion object{
        private val PARAM_CHAT_ID = "Chat id"
        private val PARAM_IMAGE_URL = "Image url"
        private val PARAM_OTHER_USER_ID = "Other user id"
        private val PARAM_CHAT_NAME = "Chat name"
        fun newIntent(context: Context?, chatId: String?, imageUrl:String?, otherUserId:String?, chatName:String?): Intent{
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(PARAM_CHAT_ID, chatId)
            intent.putExtra(PARAM_IMAGE_URL, imageUrl)
            intent.putExtra(PARAM_OTHER_USER_ID, otherUserId)
            intent.putExtra(PARAM_CHAT_NAME, chatName)
            return intent
        }
    }
}