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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_login)

        setTextChangeListener(ed_email, tif_email)
        setTextChangeListener(ed_password, tif_password)
        progessLayout.setOnTouchListener { v, event -> true }
    }

    private fun setTextChangeListener(ed:EditText, til:TextInputLayout){
        ed.addTextChangedListener (object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                til.isErrorEnabled = false
            }

        })
    }

    fun onLogin(view: View){
        var proceed = true
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
            firebaseAuth.signInWithEmailAndPassword(ed_email.text.toString(), ed_password.text.toString())
                .addOnCompleteListener { task ->
                    if(!task.isSuccessful){
                        progessLayout.visibility = View.GONE
                        Toast.makeText(this, "Login error ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    progessLayout.visibility = View.GONE
                    e.printStackTrace()
                }
        }
    }

    fun onSignup(view: View){
        startActivity(SignupActivity.newintent(this))
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
        fun newintent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}