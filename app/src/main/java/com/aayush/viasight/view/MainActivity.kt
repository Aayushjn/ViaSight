package com.aayush.viasight.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aayush.viasight.R
import com.aayush.viasight.util.INTENT_ACTION_NOTIFICATION
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val onNotice = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val pack = intent.getStringExtra("package")
            val title = intent.getStringExtra("title")
            val text = intent.getStringExtra("text")

            val tr = TableRow(applicationContext)
            tr.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            val textView = TextView(applicationContext)
            textView.layoutParams =
                TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
            textView.textSize = 20f
            textView.setTextColor(Color.parseColor("#0B0719"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.text = Html.fromHtml("$pack<br><b>$title : </b>$text", Html.FROM_HTML_MODE_LEGACY)
            }
            else {
                textView.text = Html.fromHtml("$pack<br><b>$title : </b>$text")
            }
            tr.addView(textView)
            tab.addView(tr)

            Timber.d("$pack: $title: $text")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(onNotice, IntentFilter(INTENT_ACTION_NOTIFICATION))
    }

    fun initAudioRecord() {

    }

    fun readNotifications() {

    }

    fun stopReadingNotifications() {

    }
}
