package com.ds.whatsappclone.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ds.whatsappclone.R
import com.ds.whatsappclone.fragments.ChatsFragment
import com.ds.whatsappclone.fragments.StatusListFragment
import com.ds.whatsappclone.fragments.StatusUpdateFragment
import com.ds.whatsappclone.listeners.FailureCallback
import com.ds.whatsappclone.util.*
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), FailureCallback {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var sectionPagerAdapter: SectionPagerAdapter? = null

    private val chatsFragment = ChatsFragment()
    private val statusUpdateFragment = StatusUpdateFragment()
    private val statusListFragment = StatusListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatsFragment.setFailureCallbackListener(this)

        setSupportActionBar(toolbar)
        sectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)
        container.adapter = sectionPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        resizeTab()
        tabs.getTabAt(1)?.select()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> {fab.hide()}
                    1 -> {fab.show()}
                    2 -> {
                        fab.hide()
                        statusListFragment.onVisible()
                    }
                }
            }

        })
    }

    private fun resizeTab(){
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    fun onNewChat(view: View){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            // Permission not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(this)
                    .setTitle("Contact permission")
                    .setMessage("This app requires access to your contact to initiate a conversation")
                    .setPositiveButton("Ask me") { dialog, which ->
                        requestContactsPermission()
                    }
                    .setNegativeButton("No") { dialog, which ->

                    }
                    .show()
            }else{
                requestContactsPermission()
            }
        }else{
            // Permission granted
            startNewActivity(REQUEST_NEW_CHAT)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST_READ_CONTACTS -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startNewActivity(REQUEST_NEW_CHAT)
                }
            }
        }
    }

    fun requestContactsPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_READ_CONTACTS)
    }

    fun startNewActivity(requestCode:Int){
        when(requestCode){
            REQUEST_NEW_CHAT -> startActivityForResult(ContactsActivity.newintent(this), REQUEST_NEW_CHAT)
            REQUEST_CODE_PHOTO -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_CODE_PHOTO -> {
                    statusUpdateFragment.storeImage(data?.data)
                }
                REQUEST_NEW_CHAT -> {
                    val name = data?.getStringExtra(PARAM_NAME) ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
                    checkNewChatUser(name, phone)
                }
            }
        }
    }

    fun checkNewChatUser(name:String, phone:String){
        if(!name.isNullOrEmpty() && !phone.isNullOrEmpty()){
            firebaseDB.collection(DATA_USERS)
                .whereEqualTo(DATA_USER_PHONE, phone)
                .get()
                .addOnSuccessListener {
                    if(it.documents.size > 0){
                        chatsFragment.newChat(it.documents[0].id)
                    }else{
                        AlertDialog.Builder(this)
                            .setTitle("User not found")
                            .setMessage("$name does not have an account. Send them an SMS to install this app")
                            .setPositiveButton("Ok"){dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra("sms_body", "Hi, i'm using Sasashi Chat. you should install it too")
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "An error occured, Please try again later", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        if(firebaseAuth.currentUser==null){
            startActivity(LoginActivity.newintent(this))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return  true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id  = item?.itemId
//        if(id==R.id.action_logout){
//            return  true
//        }
        when(item.itemId){
            R.id.action_profile -> onProfile()
            R.id.action_logout -> onLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onLogout(){
        firebaseAuth.signOut()
        startActivity(LoginActivity.newintent(this))
        finish()
    }

    fun onProfile(){
        startActivity(ProfileActivity.newintent(this))
        finish()
    }

    inner class SectionPagerAdapter(fm :FragmentManager) : FragmentPagerAdapter(fm){
        override fun getCount(): Int {
            return 3
        }

        override fun getItem(position: Int): Fragment {
//            return PlaceholderFragment.newIntent(position + 1)
            return when(position){
                0 -> statusUpdateFragment
                1 -> chatsFragment
                2 -> statusListFragment
                else -> statusListFragment
            }
        }

    }

    companion object{
        val PARAM_NAME = "Param Name"
        val PARAM_PHONE = "Param Phone"
        fun newintent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override fun onUserError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(LoginActivity.newintent(this))
        finish()
    }

//    class PlaceholderFragment : Fragment(){
//        override fun onCreateView(
//            inflater: LayoutInflater,
//            container: ViewGroup?,
//            savedInstanceState: Bundle?
//        ): View? {
//            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
//            rootView.section_label.text = "hellow world from section ${arguments?.getInt(
//                ARG_SECTION_NUMBER
//            )}"
//            return rootView
//        }
//        companion object{
//            private val ARG_SECTION_NUMBER = "Section Number"
//
//            fun newIntent(sectionNumber : Int): PlaceholderFragment {
//                val fragment = PlaceholderFragment()
//                val args = Bundle()
//                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
//                fragment.arguments = args
//                return  fragment
//            }
//        }
//    }

}