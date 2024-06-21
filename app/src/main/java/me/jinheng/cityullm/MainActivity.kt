package me.jinheng.cityullm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import me.jinheng.cityullm.databinding.ActivityMainBinding
import me.jinheng.cityullm.models.LLama
import me.jinheng.cityullm.models.ModelOperation
import me.jinheng.cityullm.ui.home.HomeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LLama.initFolder(getExternalFilesDir(null))

        LLama.copyCpp(this, getExternalFilesDir(null))
        if (LLama.findModel(getExternalFilesDir(null))) {
            showDownloadDialog()
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
            val chatId = System.currentTimeMillis().toString()
            val chatTitle = "Chat $chatId"
            val chatRecord = ChatRecord(chatId, chatTitle)
            homeViewModel.addChatRecord(chatRecord)

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            startActivity(intent)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun showDownloadDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
        val progressTextView: TextView = dialogView.findViewById(R.id.textViewProgressInDialog)

        // Create and show the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("Download Model")
        builder.setMessage("Could not find model in local, download a model？")
        builder.setPositiveButton("Download", null) // 先不设置监听器
        builder.setNegativeButton("Cancel", null) // 同样先不设置监听器
        val progressDialog: AlertDialog = builder.create()
        progressDialog.setCancelable(false); // 设置对话框不可通过点击外部取消，只能通过按钮

        progressDialog.show()

        // 设置按钮的点击事件，这样可以确保对话框已经显示
        progressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            progressTextView.text = "Starting download..."
            ModelOperation.downloadModelAsync("ggml-model-tinyllama", progressTextView) {
                progressDialog.dismiss()
            }
        }
        progressDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            progressDialog.dismiss()
        }

//        builder.setPositiveButton("Download") { dialog, _ ->
//            progressTextView.setText("abcd")
//            ModelOperation.downloadModelAsync("Llama-2-7B-Chat-GGUF", progressTextView) {
//                dialog.dismiss()
//            }
//        }
//        builder.setNegativeButton("Cancel") { dialog, _ ->
//            dialog.dismiss()
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // 在这里添加打开设置页面的代码
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true // 表示菜单项已经处理完毕
            }
            else -> super.onOptionsItemSelected(item) // 其他未处理的菜单项
        }
    }
}