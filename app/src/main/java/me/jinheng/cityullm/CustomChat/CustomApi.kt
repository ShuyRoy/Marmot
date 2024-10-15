package me.jinheng.cityullm.CustomChat

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Objects

object CustomApi {
    private var toast: Toast? = null
    var chatItems: ArrayList<ChatItem>? = null
    var sharedPreferences: SharedPreferences? = null
    var RequestTimeout: Long = 3600
    var max_token: Int = 1000
    var max_history: Int = 30
    var temperature: Double = 0.5
    var model: String = "text-davinci-003"
    var use_vps: String = "None"
    var stream: Boolean = true
    fun showMsg(ct: Context?, s: String?) {
        Thread {
            try {
                if (Looper.myLooper() == null) {
                    Looper.prepare()
                    if (toast != null) {
                        toast!!.cancel()
                        toast = null
                    }
                    toast =
                        Toast.makeText(ct, s, Toast.LENGTH_SHORT)
                    toast!!.show()
                    Looper.loop()
                } else {
                    if (toast != null) {
                        toast!!.cancel()
                        toast = null
                    }
                    toast =
                        Toast.makeText(ct, s, Toast.LENGTH_SHORT)
                    toast!!.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun setFullscreen(activity: AppCompatActivity) {
        Objects.requireNonNull(activity.supportActionBar)?.hide()
        if (activity.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE && Build.VERSION.SDK_INT >= 28
        ) {
            val lp = activity.window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val _window = activity.window
        val params = _window.attributes
        params.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        _window.attributes = params
        activity.window.statusBarColor = Color.TRANSPARENT
    }
}