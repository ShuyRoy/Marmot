package me.jinheng.cityullm

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
import me.jinheng.cityullm.CustomChat.CMainActivity
import me.jinheng.cityullm.CustomChat.CustomApi
import me.jinheng.cityullm.CustomChat.CustomChat
import me.jinheng.cityullm.databinding.ActivityMainBinding
import me.jinheng.cityullm.models.Config
import me.jinheng.cityullm.models.LLama
import me.jinheng.cityullm.models.ModelOperation
import me.jinheng.cityullm.ui.home.HomeViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
                CustomApi.LoadingDialogUtils.dismiss()
            }.start()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LLama.initFolder(getExternalFilesDir(null))
        ModelOperation.updateModels()
        CustomApi.LoadingDialogUtils.show(this@MainActivity, "Loading Data...")
        try {
            // val initialModelName = "ggml-model-tinyllama-1.1b-chat-v1.0-q4_0.gguf"
            val initialModelName = "ggml-model-tinyllama.gguf"
            val modelInfoName = "models.json"
            val assetManager = assets
            val file = assetManager.list("")
            for (f:String in file!!){
                println(f)
            }
            if (file?.contains(initialModelName) == true) {
                copyFileFromAssets(assetManager, initialModelName, Config.modelPath)
            }
            if (file?.contains(modelInfoName) == true) {
                copyFileFromAssets(assetManager, modelInfoName, Config.modelPath)
            }
        } catch (e: IOException) {
            e.printStackTrace();
        }

        if (!LLama.hasInitialModel()) {
            showDownloadDialog()
        }
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

        showJump2CustomChat(this@MainActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun showJump2CustomChat(ctx: Context){
        val b =
        AlertDialog.Builder(this)
        b.setTitle("是否跳转？")
            b.setNegativeButton(
            "取消"
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        b.setPositiveButton(
            "跳转"
        ) { _: DialogInterface?, _: Int ->
            ctx.startActivity(Intent(ctx, CMainActivity::class.java))
        }
        b.show()
    }

    private fun showDownloadDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
        val progressTextView: TextView = dialogView.findViewById(R.id.textViewProgressInDialog)

        // Create and show the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("Download Model")
        builder.setMessage("Could not find model in local, download tinyllama-1.1b-chat-v1.0 and have a try？")
        builder.setPositiveButton("Download", null) // 先不设置监听器
        builder.setNegativeButton("Cancel", null) // 同样先不设置监听器
        val progressDialog: AlertDialog = builder.create()
        progressDialog.setCancelable(false); // 设置对话框不可通过点击外部取消，只能通过按钮
        progressDialog.show()

        // 设置按钮的点击事件，这样可以确保对话框已经显示
        progressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            progressTextView.text = "Starting download..."
            ModelOperation.downloadModelAsync("tinyllama-1.1b-chat-v1.0", progressTextView) {
                progressDialog.dismiss()
            }
        }
        progressDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            progressDialog.dismiss()
        }
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