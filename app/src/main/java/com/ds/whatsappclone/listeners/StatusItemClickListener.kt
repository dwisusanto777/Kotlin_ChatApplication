package com.ds.whatsappclone.listeners

import com.ds.whatsappclone.util.StatusListElement

interface StatusItemClickListener {
    fun onItemClicked(statusListElement: StatusListElement)
}