package com.aayush.viasight.view

import android.Manifest
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.preference.PreferenceManager
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.RelativeSizeSpan
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aayush.viasight.R
import com.aayush.viasight.model.NotificationInfo
import com.aayush.viasight.util.*
import com.aayush.viasight.util.listener.SwipeGestureListener
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*


class MainActivity: AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private var notifications = mutableSetOf<NotificationInfo>()
    private var tts: TextToSpeech? = null
    private lateinit var gestureDetector: GestureDetector
    private lateinit var speechRecognizer: SpeechRecognizer

    private var sentToSettings = false

    private val onNotice = NotificationBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        checkPermissions()
        if (isLaunchedFirstTime(sharedPreferences)) {
            startTutorial()
            setLaunchedFirstTime(sharedPreferences)
        }

        if (!NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
                .contains(applicationContext.packageName)) {
            Toast.makeText(this, "Enable notification access", Toast.LENGTH_SHORT).show()
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        val gestureListener = SwipeGestureListener()
        gestureListener.setActivity(this)
        gestureDetector = GestureDetector(this, gestureListener)
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(onNotice, IntentFilter(INTENT_ACTION_NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()

        tts?.apply {
            if (this.isSpeaking) {
                this.stop()
            }
            this.shutdown()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        tts?.shutdown()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(ev)
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Need audio recording permission")
                        .setMessage("The app needs this permission to use speech recognition")
                        .setPositiveButton("Grant") { dialog, _ ->
                            dialog.cancel()
                            ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                PERMISSION_RECORD_AUDIO
                            )
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    builder.show()
                } else {
                    Toast.makeText(this, "Unable to get permission", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == PERMISSION_CALL_PHONE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Need permission to make calls")
                        .setMessage("The app needs this permission to make calls")
                        .setPositiveButton("Grant") { dialog, _ ->
                            dialog.cancel()
                            ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.CALL_PHONE),
                                PERMISSION_CALL_PHONE
                            )
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    builder.show()
                } else {
                    Toast.makeText(this, "Unable to get permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_SETTINGS_REQUEST) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            } else {
                proceedAfterPermission()
            }
        }
    }

    override fun onPostResume() {
        super.onPostResume()

        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            } else {
                proceedAfterPermission()
            }
        }
    }

    fun initSpeechRecognition() {
        Timber.d("Starting audio recording!")

        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(object: RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onPartialResults(partialResults: Bundle?) { }

                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onBeginningOfSpeech() {}

                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {}

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    processResult(matches?.get(0))
                }
            })

            speechRecognizer.startListening(speechIntent)
        }
    }

    fun readNotifications() {
        Timber.d("Reading notifications!")
        if (!notifications.isEmpty()) {
            val iterator = notifications.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().isRead) iterator.remove()
            }
        }
        if (!notifications.isEmpty()) {
            Timber.d(notifications.toString())
            tts = TextToSpeech(this, TextToSpeech.OnInitListener {
                if (it == TextToSpeech.SUCCESS) {
                    val result = this.tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        for (notification in notifications) {
                            if (!notification.isRead) {
                                speak(
                                    "Title: ${notification.title}\nText: ${notification.text}",
                                    UTTERANCE_ID_NOTIFICATION
                                )
                                notification.isRead = true
                            }
                        }
                    }
                }
                else {
                    Timber.e("Initialization failed")
                }
            })
        } else {
            initTTS("You do not have any pending notifications", UTTERANCE_ID_NOTIFICATION)
        }
    }

    fun stopReadingNotifications() {
        Timber.d("Stopping TTS")

        tts?.apply {
            if (this.isSpeaking) {
                this.stop()
            }
        }
    }

    private fun startTutorial() {
        vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, POSITIVE_WAVEFORM)
        initTTS(getString(R.string.tutorial_string), UTTERANCE_ID_TUTORIAL)
    }

    private fun initTTS(message: String, utteranceId: String) {
        if (tts == null) {
            tts = TextToSpeech(
                this,
                TextToSpeech.OnInitListener {
                    if (it == TextToSpeech.SUCCESS) {
                        val result = this.tts?.setLanguage(Locale.US)
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show()
                        } else {
                            speak(message, UTTERANCE_ID_DATE_TIME)
                        }
                    } else {
                        Timber.e("Initialization failed")
                    }
                }
            )
        } else {
            speak(message, utteranceId)
        }
    }

    private fun speak(message: String, utteranceId: String) =
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

    private fun processResult(match: String?) {
        Timber.d(match)

        if (match?.indexOf("what", ignoreCase = true) != -1) {
            when {
                match?.indexOf("time", ignoreCase = true) != -1 -> {
                    vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, POSITIVE_WAVEFORM)
                    initTTS("The time is " +
                            DateUtils.formatDateTime(this, Date().time, DateUtils.FORMAT_SHOW_TIME),
                        UTTERANCE_ID_DATE_TIME)
                }
                match.indexOf("date", ignoreCase = true) != -1 -> {
                    vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, POSITIVE_WAVEFORM)
                    initTTS("The date is " +
                            DateUtils.formatDateTime(this, Date().time, DateUtils.FORMAT_SHOW_DATE),
                        UTTERANCE_ID_DATE_TIME)
                }
                match.indexOf("battery percentage", ignoreCase = true) != -1 -> {
                    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, POSITIVE_WAVEFORM)
                    initTTS("The battery is at $batteryLevel%", UTTERANCE_ID_MISC)
                }
                else -> {
                    vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, NEGATIVE_WAVEFORM)
                    initTTS("I didn't quite get that! Could you repeat it?", UTTERANCE_ID_MISC)
                }
            }
        } else if (match.indexOf("call", ignoreCase = true) != -1) {
            val contacts = getContacts(contentResolver)
            val name: String
            try {
                name = match.substring(5)
            } catch (e: StringIndexOutOfBoundsException) {
                vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, NEGATIVE_WAVEFORM)
                initTTS("You didn't tell the name of a contact!", UTTERANCE_ID_MISC)
                return
            }
            Timber.d(name)
            Timber.d(contacts.toString())

            var contactFound = false
            contacts.forEach {
                if (name.equals(it.name, true)) {
                    Timber.d(it.toString())
                    contactFound = true
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse("tel:" + it.phoneNumber)
                    startActivity(callIntent)
                }
            }
            if (!contactFound) {
                vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, NEGATIVE_WAVEFORM)
                initTTS("Contact not found. Are you sure you said that right?", UTTERANCE_ID_MISC)
            }
        } else if (match.indexOf("open", ignoreCase = true) != -1) {
            val appList = getInstalledApps(packageManager)
            val appName: String
            try {
                appName = match.substring(5)
            }
            catch (e: StringIndexOutOfBoundsException) {
                vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, NEGATIVE_WAVEFORM)
                initTTS("You didn't tell the name of an app!", UTTERANCE_ID_MISC)
                return
            }
            Timber.d(appName)

            var appFound = false
            appList.forEach {
                if (appName.equals(it.appName, true)) {
                    Timber.d(it.toString())
                    appFound = true
                    startActivity(it.launchIntent)
                    vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, POSITIVE_WAVEFORM)
                    initTTS("Opening $appName", UTTERANCE_ID_MISC)
                }
            }
            if (!appFound) {
                vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, NEGATIVE_WAVEFORM)
                initTTS("App not found. Are you sure you said that right?", UTTERANCE_ID_MISC)
            }
        } else if (match.indexOf("remove", ignoreCase = true) != -1) {
            if (match.indexOf("notifications", ignoreCase = true) != -1) {
                notifications.clear()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
                vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, POSITIVE_WAVEFORM)
                initTTS("Notifications cleared", UTTERANCE_ID_NOTIFICATION)
            }
        } else if (match.indexOf("play") != -1) {
            if (match.indexOf("tutorial") != -1) {
                startTutorial()
            }
        } else if (match.indexOf("set") != -1) {
            if (match.indexOf("volume") != -1) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                when {
                    match.indexOf("silent") != -1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    match.indexOf("vibrate") != -1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    match.indexOf("normal") != -1 -> audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
            }
        } else {
            vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, NEGATIVE_WAVEFORM)
            initTTS("I didn't quite get that! Could you repeat it?", UTTERANCE_ID_MISC)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) -> {
                        val builder = AlertDialog.Builder(this)
                            .setTitle("Need audio recording permission")
                            .setMessage("The app needs this permission to use speech recognition")
                            .setPositiveButton("Grant") { dialog, _ ->
                                dialog.cancel()
                                ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.RECORD_AUDIO),
                                    PERMISSION_RECORD_AUDIO
                                )
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        builder.show()
                    }
                    sharedPreferences.getBoolean(Manifest.permission.RECORD_AUDIO, false) -> {
                        val builder = AlertDialog.Builder(this)
                            .setTitle("Need audio recording permission")
                            .setMessage("The app needs this permission to use speech recognition")
                            .setPositiveButton("Grant") { dialog, _ ->
                                dialog.cancel()
                                sentToSettings = true
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST)
                                Toast.makeText(this,
                                    "Go to permission to grant audio permission",
                                    Toast.LENGTH_LONG)
                                    .show()
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        builder.show()
                    }
                    else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_RECORD_AUDIO)
                }
            }
            sharedPreferences.edit()
                .putBoolean(Manifest.permission.RECORD_AUDIO, true)
                .apply()
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE) -> {
                        val builder = AlertDialog.Builder(this)
                            .setTitle("Need permission to make calls")
                            .setMessage("The app needs this permission to make calls")
                            .setPositiveButton("Grant") { dialog, _ ->
                                dialog.cancel()
                                ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.CALL_PHONE),
                                    PERMISSION_CALL_PHONE
                                )
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        builder.show()
                    }
                    sharedPreferences.getBoolean(Manifest.permission.CALL_PHONE, false) -> {
                        val builder = AlertDialog.Builder(this)
                            .setTitle("Need permission to make calls")
                            .setMessage("The app needs this permission to make calls")
                            .setPositiveButton("Grant") { dialog, _ ->
                                dialog.cancel()
                                sentToSettings = true
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST)
                                Toast.makeText(this,
                                    "Go to permission to grant permission to make calls",
                                    Toast.LENGTH_LONG)
                                    .show()
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        builder.show()
                    }
                    else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE),
                        PERMISSION_CALL_PHONE)
                }
            }
            sharedPreferences.edit()
                .putBoolean(Manifest.permission.CALL_PHONE, true)
                .apply()
        } else {
            proceedAfterPermission()
        }
    }

    private fun proceedAfterPermission() =
        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()

    inner class NotificationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notificationInfo = intent.getParcelableExtra<NotificationInfo>(EXTRA_NOTIFICATION)
            notifications.add(notificationInfo)

            val notificationString = "Title: ${notificationInfo.title}\nText: ${notificationInfo.text}"
            val spannableStringBuilder = SpannableStringBuilder(notificationString).apply {
                setSpan(
                    RelativeSizeSpan(2f),
                    0,
                    6,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    RelativeSizeSpan(1.5f),
                    6,
                    notificationString.indexOf("\n"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    RelativeSizeSpan(2f),
                    notificationString.indexOf("Text"),
                    notificationString.indexOf("Text") + 5,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    RelativeSizeSpan(1.5f),
                    notificationString.indexOf("Text") + 6,
                    notificationString.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val tr = TableRow(applicationContext)
            tr.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            val textView = TextView(applicationContext)
            textView.layoutParams =
                TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
            textView.textSize = 20f
            textView.setTextColor(Color.parseColor("#000000"))
            textView.text = spannableStringBuilder
            tr.addView(textView)
            tab.addView(tr)

            Timber.d(notificationInfo.toString())
        }
    }
}
