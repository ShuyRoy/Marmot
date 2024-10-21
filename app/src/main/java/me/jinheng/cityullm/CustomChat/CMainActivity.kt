package me.jinheng.cityullm.CustomChat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import me.jinheng.cityullm.R
import me.jinheng.cityullm.models.LLama

class CMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CustomApi.setFullscreen(this@CMainActivity)
        enableEdgeToEdge()
        setContentView(R.layout.custom_activity_main)
        LLama.initFolder(getExternalFilesDir(null))
        findViewById<TextView>(R.id.btm_main_custom_chat).setOnClickListener{
            startActivity(Intent(this@CMainActivity, CustomChat::class.java))
        }
    }
}