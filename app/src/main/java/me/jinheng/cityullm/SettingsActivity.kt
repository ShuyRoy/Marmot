package me.jinheng.cityullm

//import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import me.jinheng.cityullm.models.Config


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val messageEditText = findViewById<EditText>(R.id.threadNum)
        val messageEditTextMemorySize = findViewById<EditText>(R.id.memorySize)

        val spinner: Spinner = findViewById(R.id.CPUGPU)
        // 使用 ArrayAdapter 和 simple_spinner_item
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_data,
            android.R.layout.simple_spinner_item
        )
        // 设置下拉视图样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            val message = messageEditText.getText().toString()
            val messageMemorySize = messageEditTextMemorySize.getText().toString()
            val CPUGPU = spinner.selectedItem.toString()

            Config.threadNum = message.toInt()
            Config.maxMemorySize = messageMemorySize.toInt()
            Config.CPUGPU = CPUGPU

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}