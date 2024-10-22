package me.jinheng.cityullm.CustomChat

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import me.jinheng.cityullm.R
import me.jinheng.cityullm.models.Config
import me.jinheng.cityullm.models.LLama
import me.jinheng.cityullm.models.ModelInfo
import me.jinheng.cityullm.models.ModelOperation
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class CustomChat : AppCompatActivity() {
    var dataloaded = false
    var selectedNum: Int = -1
    var isBotTalking: Boolean = false
    var history: ArrayList<String>? = null
    var current_bot_chat: ChatItem? = null
    var customChatListAdapter: CustomChatListAdapter? = null
    var result: ListView? = null
    var input: EditText? = null
    var help: ImageView? = null
    var goback: ImageView? = null
    var start: ImageView? = null
    var config: ImageView? = null
    var del_history: ImageView? = null
    var mBackPressed: Long = 0
    var info: TextView? = null
    var models: List<ModelInfo>? = null
    var textView_model_name: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_activity_chat)
        CustomApi.setFullscreen(this)
        CustomApi.chatItems = ArrayList()
        selectedNum = intent.getIntExtra("Selected", 0)
        history = ArrayList()
        result = findViewById(R.id.result)
        info = findViewById(R.id.chat_info)
        textView_model_name = findViewById(R.id.chat_model_name)
        customChatListAdapter = CustomChatListAdapter(this)
        result!!.adapter = customChatListAdapter
        input = findViewById(R.id.input)
        //help = findViewById(R.id.help)
        goback = findViewById(R.id.goback_page)
        start = findViewById(R.id.start)
        config = findViewById(R.id.config)
        del_history = findViewById(R.id.del_history)
        del_history!!.setOnClickListener {
            CustomApi.chatItems!!.clear()
            refreshListview()
            //LLama.clear()
        }
        //help!!.setOnClickListener {
            //showHelp()
        //}
        goback!!.setOnClickListener {
            LLama.destroy()
            finish()
        }
        config!!.setOnClickListener {
            showConfig()
        }
        start!!.setOnClickListener {
            val str:String = input!!.text.toString().trim()
            if(str.isNotEmpty()){
                println("INPUT STR: $str")
                userMsg(str)
            }
        }
        loadData()
        textView_model_name!!.text = models!![selectedNum].modelName
        botEnd()
    }

    fun checkFilesInAssets(files: Array<String>?, name: String): Boolean{
        for (f in files!!){
            if (f.contains(name)){
                return true
            }
        }
        return false
    }

    private fun loadData() {
        models = ModelOperation.getAllSupportModels()
        try {
            // val initialModelName = "ggml-model-tinyllama-1.1b-chat-v1.0-q4_0.gguf"
            val assetManager = assets
            val file = assetManager.list("")
            var model_name = models!![selectedNum].modelName
            if (checkFilesInAssets(file, models!![selectedNum].modelName)) {
                if (model_name.contains("tinyllama-1.1b-chat")){
                    model_name = "ggml-model-tinyllama-1.1b-chat-v1.0-q4_0.gguf"
                }
                else if (!model_name.startsWith("ggml")){
                    model_name = "ggml-model-$model_name.gguf"
                }

                CustomApi.LoadingDialogUtils.show(this@CustomChat, "Loading Model...")
                Thread{
                    copyFileFromAssets(this@CustomChat,
                        model_name,
                        Config.modelPath)
                    CustomApi.LoadingDialogUtils.dismiss()

                    LLama.init(ModelOperation.getAllSupportModels()[selectedNum].modelName,
                        ModelOperation.getAllSupportModels()[selectedNum].modelLocalPath,
                        this@CustomChat)
                }.start()
            }else{
                println("NOT FOUND: $model_name")
            }
        } catch (e: IOException) {
            e.printStackTrace();
        }

    }

    fun closeInputMethod() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            val v = window.peekDecorView()
            if (null != v) {
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    fun refreshListview() {
        closeInputMethod()
        customChatListAdapter!!.notifyDataSetChanged()
        result!!.adapter = customChatListAdapter
    }

    fun showHelp() {
        val b = AlertDialog.Builder(this)
        val view = View.inflate(this, R.layout.custom_layout_help, null)
        b.setView(view)
        b.setNegativeButton(
            "已  阅"
        ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        b.show()
    }

    fun buildPrompt(): String {
        val prompt = StringBuilder()
        if (!history!!.isEmpty()) {
            for (s in history!!) {
                prompt.append(s)
            }
        }
        prompt.append("Q: ").append(input!!.text.toString()).append("\n\nA:")
        return prompt.toString()
    }

    private fun showConfig() {
        val builder = AlertDialog.Builder(this)
        val view = View.inflate(this, R.layout.custom_layout_config, null)
        // initConfigs(view);
        builder.setView(view)
        builder.setTitle("设置")
        builder.setNegativeButton(
            "取 消"
        ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        builder.setPositiveButton(
            "确 定"
        ) { dialog: DialogInterface?, which: Int -> }.show()
    }

    fun stopPrinting() {
        LLama.stop()
    }

    fun updateInfo(msg:String){
        runOnUiThread {
            info!!.text = msg;
        }
    }

    override fun onBackPressed() {
        if (mBackPressed > System.currentTimeMillis() - 2000) {
            finish()
            super.onBackPressed()
        } else {
            CustomApi.showMsg(this, "连续返回两次退出APP")
            mBackPressed = System.currentTimeMillis()
        }
    }

    fun userMsg(msg: String){
        botEnd()
        input!!.setText("")
//        if (history!!.size >= CustomApi.max_history) {
//            history!!.removeAt(0)
//            history!!.removeAt(0)
//        }
//        history!!.add(("Q: " + msg.obj.toString()).toString() + "<|endoftext|>\n\n")
        val chatItem = ChatItem()
        chatItem.type = 1
        chatItem.text = msg
        CustomApi.chatItems!!.add(chatItem)
        refreshListview()
        LLama.CustomRun(msg)
        botBegin()
    }

    fun botBegin(){
        isBotTalking = true;
        CustomApi.chatItems!!.add(current_bot_chat!!);
        refreshListview();
    }

    fun botContinue(msg: String){
        runOnUiThread{
            current_bot_chat!!.appendText(msg);
            refreshListview();
        }
    }

    fun botEnd() {
        runOnUiThread {
            isBotTalking = false
            // history!!.add("A: $msg<|endoftext|>\n\n")
            current_bot_chat = ChatItem()
            current_bot_chat!!.type = 0
            current_bot_chat!!.text = "\t\t"
            refreshListview()
        }
    }

    @Throws(IOException::class)
    fun copyFileFromAssets(context: Context, fileName: String?, destinationPath: String) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            // 打开 assets 中的文件输入流
            println("COPYING ${fileName!!}")
            inputStream = context.assets.open(fileName)
            // 创建输出文件的输出流
            val outFile = File("$destinationPath/$fileName")
            outputStream = FileOutputStream(outFile)

            // 用于存储临时数据的缓冲区
            val buffer = ByteArray(1024)
            var read: Int
            while ((inputStream.read(buffer).also { read = it }) != -1) {
                outputStream.write(buffer, 0, read)
            }
            // 尝试设置执行权限
            if (!outFile.setExecutable(true, false)) {
                throw IOException("Failed to set execute permission for the file.")
            }
            dataloaded = true
        } finally {
            inputStream?.close()
            if (outputStream != null) {
                outputStream.flush()
                outputStream.close()
            }
            println("COPIED")
            CustomApi.LoadingDialogUtils.dismiss()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        LLama.destroy()
        // exitProcess(0)
    }

}