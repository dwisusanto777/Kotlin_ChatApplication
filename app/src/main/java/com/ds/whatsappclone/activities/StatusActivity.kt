package com.ds.whatsappclone.activities

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ds.whatsappclone.R
import com.ds.whatsappclone.listeners.ProgressListener
import com.ds.whatsappclone.util.StatusListElement
import com.ds.whatsappclone.util.populateImage
import kotlinx.android.synthetic.main.activity_status.*

class StatusActivity : AppCompatActivity(), ProgressListener {

    private lateinit var statusListElement: StatusListElement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        if(intent.hasExtra(PARAM_STATUS_ELEMENT)){
            statusListElement = intent.getParcelableExtra(PARAM_STATUS_ELEMENT)!!
        }else{
            Toast.makeText(this, "Unable to get status", Toast.LENGTH_SHORT).show()
            finish()
        }

        tv_status.text = statusListElement.status
        populateImage(this, statusListElement.statusUrl, iv_status)

        progressBar.max = 100
        TimerTask(this).execute("")

    }

    private class TimerTask(val listener:ProgressListener): AsyncTask<String, Int, Any>(){
        override fun doInBackground(vararg params: String?){
            var i = 0
            val sleep = 10L
            while (i<100){
                i++
                publishProgress(i)
                Thread.sleep(sleep)
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            if(values[0] != null){
                listener.onProgressUpdate(values[0]!!)
            }
        }
    }

    companion object{
        val PARAM_STATUS_ELEMENT = "element"
        fun getIntent(context: Context, statusListElement: StatusListElement?): Intent{
            val intent = Intent(context, StatusActivity::class.java)
            intent.putExtra(PARAM_STATUS_ELEMENT, statusListElement)
            return intent
        }
    }

    override fun onProgressUpdate(progress: Int) {
        progressBar.progress = progress
        if(progress==100){
            finish()
        }
    }
}