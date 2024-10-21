package me.jinheng.cityullm.CustomChat

import me.jinheng.cityullm.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
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
    object LoadingDialogUtils {
        private var loadingDialog: AlertDialog? = null
        private var reference: WeakReference<Activity?>? = null
        private fun init(activity: Activity?, res: Int, text: String?) {
            if (loadingDialog == null || reference == null || reference!!.get() == null || reference!!.get()!!.isFinishing
            ) {
                reference = WeakReference(activity)
                loadingDialog = AlertDialog.Builder(reference!!.get()).create()
                if (res > 0) {
                    val view = LayoutInflater.from(activity).inflate(res, null)
                    if (null != text) {
                        val textView = view.findViewById<TextView>(R.id.loading_text)
                        textView.text = text
                    }
                    loadingDialog!!.setView(view)
                } else {
                    loadingDialog!!.setMessage("加载中...")
                }
                loadingDialog!!.setCancelable(false)
            }
        }

        private fun setCancelable(b: Boolean) {
            if (loadingDialog == null) return
            loadingDialog!!.setCancelable(b)
        }

        /**
         * 显示等待框
         */
        fun show(act: Activity?) {
            show(act, R.layout.custom_dialog_loading, null, false)
        }

        fun show(act: Activity?, text: String?) {
            show(act, R.layout.custom_dialog_loading, text, false)
        }

        fun show(activity: Activity?, res: Int, text: String?, isCancelable: Boolean) {
            dismiss()
            init(activity, res, text)
            setCancelable(isCancelable)
            loadingDialog!!.show()
        }

        /**
         * 隐藏等待框
         */
        fun dismiss() {
            if (loadingDialog != null && loadingDialog!!.isShowing) {
                loadingDialog!!.dismiss()
                loadingDialog = null
                reference = null
            }
        }
    }

    private fun copyFileFromAssets(assetManager: AssetManager,
                                   initialModelName: String,
                                   modelDir: String): Boolean {
        return try {
            val inputStream = assetManager.open(initialModelName)
            val outFile = File(modelDir + initialModelName)
            val outputStream = FileOutputStream(outFile)
            Thread {
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.flush()
                outputStream.close()
                LoadingDialogUtils.dismiss()
            }.start()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}