package com.ds.whatsappclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ds.whatsappclone.R
import com.ds.whatsappclone.listeners.StatusItemClickListener
import com.ds.whatsappclone.util.StatusListElement
import com.ds.whatsappclone.util.populateImage

class StatusListAdapter(val statusList: ArrayList<StatusListElement>) : RecyclerView.Adapter<StatusListAdapter.StatusListViewHolder>() {

    private var clickListener:StatusItemClickListener? = null

    class StatusListViewHolder(view:View): RecyclerView.ViewHolder(view){
        private var layout = view.findViewById<RelativeLayout>(R.id.itemLayout)
        private var elementIv = view.findViewById<ImageView>(R.id.iv_item)
        private var elementNameTv = view.findViewById<TextView>(R.id.tv_itemName)
        private var elementTimeTv = view.findViewById<TextView>(R.id.tv_itemTime)

        fun bind(element: StatusListElement, listener: StatusItemClickListener?){
            populateImage(elementIv.context, element.userUrl, elementIv, R.drawable.default_user)
            elementNameTv.text = element.userName
            elementTimeTv.text = element.statusTime
            layout.setOnClickListener {
                listener?.onItemClicked(element)
            }
        }
    }

    fun onRefresh(){
        statusList.clear()
        notifyDataSetChanged()
    }

    fun addElement(element: StatusListElement){
        statusList.add(element)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= StatusListViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.item_status_list, parent, false)
    )

    override fun onBindViewHolder(holder: StatusListViewHolder, position: Int) {
        holder.bind(statusList[position], clickListener)
    }

    fun setOnItemClickListener(listener: StatusItemClickListener?){
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return statusList.size
    }

}