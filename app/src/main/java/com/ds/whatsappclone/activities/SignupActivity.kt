package com.ds.whatsappclone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.ds.whatsappclone.R
import com.ds.whatsappclone.util.DATA_USERS
import com.ds.whatsappclone.util.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.ed_email
import kotlinx.android.synthetic.main.activity_signup.ed_password
import kotlinx.android.synthetic.main.activity_signup.progessLayout
import kotlinx.android.synthetic.main.activity_signup.tif_email
import kotlinx.android.synthetic.main.activity_signup.tif_password

class SignupActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val fireBaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if(user!=null){
            startActivity(MainActivity.newintent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_signup)

        setTextChangeListener(ed_name, tif_name)
        setTextChangeListener(ed_phone, tif_phone)
        setTextChangeListener(ed_email, tif_email)
        setTextChangeListener(ed_password, tif_password)
        progessLayout.setOnTouchListener { v, event -> true }
    }

    private fun setTextChangeListener(ed: EditText, til: TextInputLayout){
        ed.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                til.isErrorEnabled = false
            }
        })
    }

    fun onSignup(view: View){
        var proceed = true
        if(ed_name.text.isNullOrEmpty()){
            proceed = false
            tif_name.error = "Email is required"
            tif_name.isErrorEnabled = true
        }
        if(ed_phone.text.isNullOrEmpty()){
            proceed = false
            tif_phone.error = "Password is required"
            tif_phone.isErrorEnabled = true
        }
        if(ed_email.text.isNullOrEmpty()){
            proceed = false
            tif_email.error = "Email is required"
            tif_email.isErrorEnabled = true
        }
        if(ed_password.text.isNullOrEmpty()){
            proceed = false
            tif_password.error = "Password is required"
            tif_password.isErrorEnabled = true
        }
        if(proceed){
            progessLayout.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(ed_email.text.toString(), ed_password.text.toString())
                .addOnCompleteListener {task ->
                    if(!task.isSuccessful){
                        Toast.makeText(this, "Login error ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }else if(firebaseAuth.uid != null) {
                        val email = ed_email.text.toString()
                        val phone = ed_phone.text.toString()
                        val name = ed_name.text.toString()
                        val password = ed_password.text.toString()
                        val user = User(email, phone, name, "", "NEW USER", "", "")
                        firebaseDB.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                    }
                    progessLayout.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    progessLayout.visibility = View.GONE
                    e.printStackTrace()
                }
        }
    }

    fun onLogin(view: View){
        startActivity(LoginActivity.newintent(this))
        finish()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(fireBaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener (fireBaseAuthListener)
    }

    companion object{
        fun newintent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}