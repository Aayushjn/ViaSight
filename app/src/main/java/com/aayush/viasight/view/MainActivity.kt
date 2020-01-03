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
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.aayush.viasight.R
import com.aayush.viasight.ViaSightApplication
import com.aayush.viasight.model.NotificationInfo
import com.aayush.viasight.util.android.*
import com.aayush.viasight.util.android.listener.SwipeGestureListener
import com.aayush.viasight.util.common.*
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*

class MainActivity: AppCompatActivity() {
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private var notifications: MutableSet<NotificationInfo> = mutableSetOf()
    private val tts: TextToSpeech by lazy {
        TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                val ttsLang: Int = tts.setLanguage(ViaSightApplication.defaultLocale)
                if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Timber.e("Language not supported")
                } else {
                    Timber.i("Language supported")
                }
            } else {
                toast("TTS initialization failed")
            }
        }
    }
    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(this, SwipeGestureListener(this))
    }
    private lateinit var speechRecognizer: SpeechRecognizer

    private var sentToSettings = false
    private val onNotice: NotificationBroadcastReceiver = NotificationBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        if (isLaunchedFirstTime(sharedPreferences)) {
            startTutorial()
            setLaunchedFirstTime(sharedPreferences)
        }
        notifications = getNotifications(sharedPreferences)

        if (!NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
                .contains(applicationContext.packageName)) {
            toast("Enable notification access")
            startActivity(
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(onNotice, IntentFilter(INTENT_ACTION_NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()

        with(tts) {
            if (isSpeaking) stop()
            shutdown()
        }
        saveNotifications(sharedPreferences, notifications)
    }

    override fun onDestroy() {
        super.onDestroy()

        tts.shutdown()
        saveNotifications(sharedPreferences, notifications)
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
                    AlertDialog.Builder(this)
                        .setTitle("Need audio recording permission")
                        .setMessage("The app needs this permission to use speech recognition")
                        .setPositiveButton("Grant") { dialog, _ ->
                            dialog.cancel()
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                PERMISSION_RECORD_AUDIO
                            )
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        .show()
                } else {
                    toast("Unable to get permission")
                }
            }
        } else if (requestCode == PERMISSION_CALL_PHONE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                    AlertDialog.Builder(this)
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
                        .show()
                } else {
                    toast("Unable to get permission")
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
                toast("Permission not granted")
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
                toast("Permission not granted")
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
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                        processResult(it[0])
                    }
                }
            })

            speechRecognizer.startListening(speechIntent)
        }
    }

    fun readNotifications() {
        Timber.d("Reading notifications!")
        if (notifications.isNotEmpty()) {
            val iterator = notifications.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().isRead) iterator.remove()
            }
        }
        if (notifications.isNotEmpty()) {
            Timber.d(notifications.toString())
            notifications.forEach {
                if (!it.isRead) {
                    tts.speak("Title: ${it.title}\nText: ${it.text}", UTTERANCE_ID_NOTIFICATION)
                    it.isRead = true
                }
            }
        } else {
            tts.speak("You do not have any pending notifications", UTTERANCE_ID_NOTIFICATION)
        }
    }

    fun stopReadingNotifications() {
        Timber.d("Stopping TTS")

        if (tts.isSpeaking) tts.stop()
    }

    private fun startTutorial() {
        getVibrator().vibrate(POSITIVE_WAVEFORM)
        tts.speak(getString(R.string.tutorial_string), UTTERANCE_ID_TUTORIAL)
    }

    private fun processResult(match: String) {
        Timber.d(match)

        if (match.indexOf("what", ignoreCase = true) != -1) {
            when {
                match.indexOf("time", ignoreCase = true) != -1 -> {
                    getVibrator().vibrate(POSITIVE_WAVEFORM)
                    tts.speak(
                        "The time is " + Date().toSpeechString(this),
                        UTTERANCE_ID_DATE_TIME
                    )
                }
                match.indexOf("date", ignoreCase = true) != -1 -> {
                    getVibrator().vibrate(POSITIVE_WAVEFORM)
                    tts.speak(
                        "The date is " + Date().toSpeechString(this),
                        UTTERANCE_ID_DATE_TIME
                    )
                }
                match.indexOf("battery percentage", ignoreCase = true) != -1 -> {
                    val batteryManager: BatteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val batteryLevel: Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    getVibrator().vibrate(POSITIVE_WAVEFORM)
                    tts.speak("The battery is at $batteryLevel%", UTTERANCE_ID_MISC)
                }
                else -> {
                    getVibrator().vibrate(NEGATIVE_WAVEFORM)
                    tts.speak("I didn't quite get that! Could you repeat it?", UTTERANCE_ID_MISC)
                }
            }
        } else if (match.indexOf("call", ignoreCase = true) != -1) {
            val contacts = contentResolver.getContacts()
            val name: String
            try {
                name = match.substring(5)
            } catch (e: StringIndexOutOfBoundsException) {
                getVibrator().vibrate(NEGATIVE_WAVEFORM)
                tts.speak("You didn't tell the name of a contact!", UTTERANCE_ID_MISC)
                return
            }
            Timber.d(name)
            Timber.d(contacts.toString())

            var contactFound = false
            contacts.forEach {
                if (name.contains(it.name, true)) {
                    Timber.d(it.toString())
                    contactFound = true
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse("tel:" + it.phoneNumber)
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                        PackageManager.PERMISSION_GRANTED) {
                        tts.speak("Grant call permission to make calls", UTTERANCE_ID_MISC)
                    }
                    startActivity(callIntent)
                }
            }
            if (!contactFound) {
                getVibrator().vibrate(NEGATIVE_WAVEFORM)
                tts.speak("Contact not found. Are you sure you said that right?", UTTERANCE_ID_MISC)
            }
        } else if (match.indexOf("open", ignoreCase = true) != -1) {
            val appList = packageManager.getInstalledApps()
            val appName: String
            try {
                appName = match.substring(5)
            }
            catch (e: StringIndexOutOfBoundsException) {
                getVibrator().vibrate(NEGATIVE_WAVEFORM)
                tts.speak("You didn't tell the name of an app!", UTTERANCE_ID_MISC)
                return
            }
            Timber.d(appName)

            var appFound = false
            appList.forEach {
                if (appName.equals(it.appName, true)) {
                    Timber.d(it.toString())
                    appFound = true
                    startActivity(it.launchIntent)
                    getVibrator().vibrate(POSITIVE_WAVEFORM)
                    tts.speak("Opening $appName", UTTERANCE_ID_MISC)
                }
            }
            if (!appFound) {
                getVibrator().vibrate(NEGATIVE_WAVEFORM)
                tts.speak("App not found. Are you sure you said that right?", UTTERANCE_ID_MISC)
            }
        } else if (match.indexOf("remove", ignoreCase = true) != -1) {
            if (match.indexOf("notifications", ignoreCase = true) != -1) {
                notifications.clear()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
                textView.text = ""
                getVibrator().vibrate(POSITIVE_WAVEFORM)
                tts.speak("Notifications cleared", UTTERANCE_ID_NOTIFICATION)
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
            getVibrator().vibrate(NEGATIVE_WAVEFORM)
            tts.speak("I didn't quite get that! Could you repeat it?", UTTERANCE_ID_MISC)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) -> {
                        AlertDialog.Builder(this)
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
                            .show()
                    }
                    sharedPreferences.getBoolean(Manifest.permission.RECORD_AUDIO, false) -> {
                        AlertDialog.Builder(this)
                            .setTitle("Need audio recording permission")
                            .setMessage("The app needs this permission to use speech recognition")
                            .setPositiveButton("Grant") { dialog, _ ->
                                dialog.cancel()
                                sentToSettings = true
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivityForResult(intent,
                                    PERMISSION_SETTINGS_REQUEST
                                )
                                Toast.makeText(this,
                                    "Go to permission to grant audio permission",
                                    Toast.LENGTH_LONG)
                                    .show()
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                            .show()
                    }
                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_RECORD_AUDIO
                    )
                }
            }
            sharedPreferences.edit { putBoolean(Manifest.permission.RECORD_AUDIO, true) }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE) -> {
                        AlertDialog.Builder(this)
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
                            .show()
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
                                startActivityForResult(intent,
                                    PERMISSION_SETTINGS_REQUEST
                                )
                                Toast.makeText(this,
                                    "Go to permission to grant permission to make calls",
                                    Toast.LENGTH_LONG)
                                    .show()
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        builder.show()
                    }
                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        PERMISSION_CALL_PHONE
                    )
                }
            }
            sharedPreferences.edit { putBoolean(Manifest.permission.CALL_PHONE, true) }
        } else {
            proceedAfterPermission()
        }
    }

    private fun proceedAfterPermission() = toast("Permission granted")

    inner class NotificationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notificationInfo: NotificationInfo? = intent.getParcelableExtra(EXTRA_NOTIFICATION)
            notificationInfo?.let {
                notifications.add(it)

                val notificationString = "Package: ${it.packageName}\nTitle: ${it.title}\nText: ${it.text}\nTime: ${it.date}\n\n"
                val spannableStringBuilder = SpannableStringBuilder(notificationString).apply {
                    setSpan(
                        RelativeSizeSpan(2f),
                        0,
                        8,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        RelativeSizeSpan(1.5f),
                        8,
                        notificationString.indexOf("Title"),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        RelativeSizeSpan(2f),
                        notificationString.indexOf("Title"),
                        notificationString.indexOf("Title") + 6,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        RelativeSizeSpan(1.5f),
                        notificationString.indexOf("Title") + 6,
                        notificationString.indexOf("Text"),
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
                        notificationString.indexOf("Text") + 5,
                        notificationString.indexOf("Time"),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        RelativeSizeSpan(2f),
                        notificationString.indexOf("Time"),
                        notificationString.indexOf("Time") + 5,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        RelativeSizeSpan(1.5f),
                        notificationString.indexOf("Time") + 5,
                        notificationString.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                textView.textSize = 20f
                textView.setTextColor(Color.parseColor("#000000"))
                textView.text = spannableStringBuilder.insert(0, textView.text)

                Timber.d(it.toString())
            }
        }
    }
}
