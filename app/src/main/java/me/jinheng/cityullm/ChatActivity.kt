package me.jinheng.cityullm

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import me.jinheng.cityullm.models.AnswerState
import me.jinheng.cityullm.models.LLama


class ChatActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modelName = intent.getStringExtra("MODEL_NAME")

        setContentView(R.layout.fragment_chat)

        // Initialize the RecyclerView and adapter
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        messageAdapter = MessageAdapter()
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Create and set the adapter for the RecyclerView

        // Set up the send button click listener
        val sendButton: Button = findViewById(R.id.sendButton)
        val messageEditText: EditText = findViewById(R.id.messageEditText)

        val fab: ExtendedFloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            if (LLama.answerState == AnswerState.ANSWERING) {
                LLama.stop();
            } else {
                LLama.clear();
            }
        }

        val speedTextView: TextView = findViewById(R.id.speedTextView)

        fab.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0f
            private var initialY = 0f
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 记录初始位置
                        initialX = view.x
                        initialY = view.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 计算位移
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            isDragging = true
                            view.x = initialX + deltaX
                            view.y = initialY + deltaY
                            view.requestLayout()
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // 如果没有拖动，认为是点击事件
                            view.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })

        // TODO: receive model name and open the corresponding model.
        LLama.init(modelName, false, messageAdapter, this, recyclerView, speedTextView, fab)
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()

            if (LLama.answering) {
                Toast.makeText(this, "Answering...", Toast.LENGTH_SHORT).show()
            } else if (message.isNotEmpty()) {
                // TODO: Handle sending the message
                messageEditText.text.clear()
                messageAdapter.messages = (messageAdapter.messages + Message(message, true)).toMutableList()
                messageAdapter.notifyItemInserted(messageAdapter.messages.size - 1);

                recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                LLama.run(message)
            }

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LLama.destroy()

                isEnabled = false
                onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }

}