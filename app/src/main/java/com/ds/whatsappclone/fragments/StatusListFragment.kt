package com.ds.whatsappclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ds.whatsappclone.R
import com.ds.whatsappclone.activities.StatusActivity
import com.ds.whatsappclone.adapters.StatusListAdapter
import com.ds.whatsappclone.listeners.StatusItemClickListener
import com.ds.whatsappclone.util.DATA_USERS
import com.ds.whatsappclone.util.DATA_USER_CHATS
import com.ds.whatsappclone.util.StatusListElement
import com.ds.whatsappclone.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_status_list.*

class StatusListFragment : Fragment(), StatusItemClickListener {

    private val firebaseFB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var statusListAdapter = StatusListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onItemClicked(statusListElement: StatusListElement) {
        startActivity(StatusActivity.getIntent(requireContext(), statusListElement))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusListAdapter.setOnItemClickListener(this)
        rv_statusList.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = statusListAdapter
            addItemDecoration(DividerItemDecoration(this@StatusListFragment.context, DividerItemDecoration.VERTICAL))
        }
    }

    fun onVisible(){
        statusListAdapter.onRefresh()
        refreshList()
    }

    fun refreshList(){
        firebaseFB.collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener { doc ->
                if(doc.contains(DATA_USER_CHATS)){
                    val partners = doc[DATA_USER_CHATS]
                    for(partner in (partners as HashMap<String, String>).keys){
                        firebaseFB.collection(DATA_USERS)
                            .document(partner)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                val partner = documentSnapshot.toObject(User::class.java)
                                if(partner != null){
                                    if(!partner.status.isNullOrEmpty() || !partner.statusUrl.isNullOrEmpty()){
                                        val newElement = StatusListElement(partner.name, partner.imageUrl, partner.status, partner.statusUrl, partner.statusTime)
                                        statusListAdapter.addElement(newElement)
                                    }
                                }
                            }
                    }
                }
            }
    }

}