package com.ds.whatsappclone.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ds.whatsappclone.R
import com.ds.whatsappclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.progessLayout

class ProfileActivity : AppCompatActivity() {

    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val fireBaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if(user!=null){
            startActivity(MainActivity.newintent(this))
            finish()
        }
    }
    private var imageUrl:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if(userId.isNullOrEmpty()){
            finish()
        }

        progessLayout.setOnTouchListener { v, event -> true }

        iv_photo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }
    }

    private fun populateInfo(){
        progessLayout.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS)
            .document(userId!!)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                imageUrl = user?.imageUrl
                ed_name.setText(user?.name, TextView.BufferType.EDITABLE)
                ed_email.setText(user?.email, TextView.BufferType.EDITABLE)
                ed_phone.setText(user?.phone, TextView.BufferType.EDITABLE)
                if(imageUrl != null){
                    populateImage(this, user?.imageUrl, iv_photo, R.drawable.default_user)
                }
                progessLayout.visibility = View.GONE
            }
            .addOnFailureListener {
                it.printStackTrace()
                finish()
            }
    }

    fun onApply(view:View){
        progessLayout.visibility = View.VISIBLE
        val name = ed_name.text.toString()
        val email = ed_email.text.toString()
        val phone = ed_phone.text.toString()
        val map = HashMap<String, Any>()
        map[DATA_USER_NAME] = name
        map[DATA_USER_EMAIL] = email
        map[DATA_USER_PHONE] = phone

        firebaseDB.collection(DATA_USERS)
            .document(userId!!)
            .update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update Successfull", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
            }
        progessLayout.visibility = View.GONE
    }

    fun onDelete(view: View){
        progessLayout.visibility = View.VISIBLE
        AlertDialog.Builder(this)
            .setTitle("Delete account")
            .setMessage("This will delete your profile information, Are you sure?")
            .setPositiveButton("Yes"){ dialog, which ->
                firebaseDB.collection(DATA_USERS).document(userId!!).delete()
                firebaseStorage.child(DATA_IMAGES).child(userId).delete()
                firebaseAuth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        finish()
                    }
                    ?.addOnFailureListener {
                        finish()
                    }
                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No"){ dialog, which ->
                progessLayout.visibility = View.GONE
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO){

        }
    }

    private fun storeImage(imageUri:Uri?){
        if(imageUri!=null){
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            progessLayout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener { taskSnapshot ->
                            val url = taskSnapshot.toString()
                            firebaseDB.collection(DATA_USERS)
                                .document(userId)
                                .update(DATA_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    populateImage(this, imageUrl, iv_photo, R.drawable.default_user)
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

    private fun onUploadFailure(){
        Toast.makeText(this, "Image upload failed, Please try again later", Toast.LENGTH_SHORT).show()
        progessLayout.visibility = View.GONE
    }

    companion object{
        fun newintent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}