package me.jinheng.cityullm.CustomChat

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import me.jinheng.cityullm.R
import me.jinheng.cityullm.models.LLama
import me.jinheng.cityullm.models.ModelOperation

class CMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CustomApi.setFullscreen(this@CMainActivity)
        enableEdgeToEdge()
        setContentView(R.layout.custom_activity_main)
        LLama.initFolder(getExternalFilesDir(null))
        findViewById<TextView>(R.id.btm_main_custom_chat).setOnClickListener{
            showModels()
        }

    }
    fun showModels() {
        val b = AlertDialog.Builder(this)
        val view = View.inflate(this, R.layout.custom_layout_model_list, null)
        view.findViewById<TextView>(R.id.model_list_text).text = "Model List"
        var model_listview =  view.findViewById<ListView>(R.id.listview_models)
        var models = ModelOperation.getAllSupportModels()
        if (models.isNotEmpty()) {
            val mainInfo = arrayOfNulls<String>(models.size)
            for (i in 0 until models.size) {
                mainInfo[i] = "\t\tModel name: ${models[i].modelName}\n\t\tModel size:${models[i].modelSize}\n\n"
            }
            model_listview.adapter =
                ArrayAdapter(this@CMainActivity, R.layout.custom_listview_item, mainInfo)
            model_listview.onItemClickListener =
                AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                    val it = Intent(
                        this@CMainActivity,
                        CustomChat::class.java
                    )
                    it.putExtra("Selected", i)
                    this@CMainActivity.startActivity(it)
                }
        }
        b.setView(view)
        b.show()
    }
}