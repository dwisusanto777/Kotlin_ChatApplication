package com.ds.whatsappclone.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ds.whatsappclone.R
import com.ds.whatsappclone.activities.MainActivity
import com.ds.whatsappclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_status_update.*


class StatusUpdateFragment : Fragment() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progessLayout.setOnTouchListener { v, event -> true }
        button_sendStatus.setOnClickListener {
            onUpdate()
        }
        populateImage(requireContext(), imageUrl, iv_status)

        statusLayout.setOnClickListener {
            if(isAdded){
                (activity as MainActivity).startNewActivity(REQUEST_CODE_PHOTO)
            }
        }
    }

    fun storeImage(imageUri : Uri?){
        if(imageUri != null && userId != null){
            Toast.makeText(activity, "Uploading...", Toast.LENGTH_SHORT).show()
            progessLayout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child("${userId}_status ")
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { taskSnapshot ->
                        val url = taskSnapshot.toString()
                        firebaseDB.collection(DATA_USERS)
                            .document(userId)
                            .update(DATA_USER_STATUS, url)
                            .addOnSuccessListener {
                                imageUrl = url
                                populateImage(requireContext(), imageUrl, iv_status)
                            }
                        progessLayout.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        onUploadFailure()
                    }
                }
                .addOnFailureListener{
                    onUploadFailure()
                }
        }
    }

    fun onUpdate(){
        progessLayout.visibility = View.VISIBLE
        val map = HashMap<String, Any>()
        map[DATA_USER_STATUS] = ed_status.text.toString()
        map[DATA_USER_STATUS_URL] = imageUrl
        map[DATA_USER_STATUS_TIME] = getTime()

        firebaseDB.collection(DATA_USERS).document(userId!!)
            .update(map)
            .addOnSuccessListener {
                progessLayout.visibility = View.GONE
                Toast.makeText(activity, "Status updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progessLayout.visibility = View.GONE
                Toast.makeText(activity, "Status update failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onUploadFailure(){
        Toast.makeText(activity, "Image upload failed, please try again later", Toast.LENGTH_SHORT).show()
        progessLayout.visibility = View.GONE
    }

}