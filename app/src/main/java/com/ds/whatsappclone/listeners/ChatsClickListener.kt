package com.ds.whatsappclone.listeners

interface ChatsClickListener {
    fun onChatClicked(name:String?, otherUserId:String?, chatImageUrl:String?, chatName:String?)
}