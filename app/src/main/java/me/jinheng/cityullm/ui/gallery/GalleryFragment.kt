package me.jinheng.cityullm.ui.gallery

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.jinheng.cityullm.ChatActivity
import me.jinheng.cityullm.ChatRecord
import me.jinheng.cityullm.R
import me.jinheng.cityullm.databinding.FragmentGalleryBinding
import me.jinheng.cityullm.models.ModelInfo
import me.jinheng.cityullm.models.ModelOperation
import me.jinheng.cityullm.ui.home.HomeViewModel
import java.text.DecimalFormat

fun bytesToGigabytes(bytes: Long): String {
    // 1 GB = 1024^3 bytes
    val gigabytes = bytes / (1024.0 * 1024.0 * 1024.0)

    // 创建 DecimalFormat 实例，用于格式化数字
    val decimalFormat = DecimalFormat("#.0")

    // 返回格式化后的 GB 值
    return decimalFormat.format(gigabytes)
}

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var models: List<ModelInfo> = ModelOperation.getAllSupportModels()
        val numberOfTextViews = models.size

        for (i in 0..<numberOfTextViews) {
            val dynamicTextView = TextView(context)

            // 设置背景和边框
            val background = GradientDrawable()
            background.shape = GradientDrawable.RECTANGLE
            background.setColor(Color.WHITE)  // 背景颜色
            background.setStroke(1, Color.LTGRAY)  // 边框颜色和宽度
            background.cornerRadius = 4f  // 边框圆角

            dynamicTextView.background = background

            dynamicTextView.setTextColor(Color.BLACK)
            dynamicTextView.textSize = 20f  // 单位是 sp

            // 添加内边距
            val padding = 16  // 像素值
            dynamicTextView.setPadding(padding, padding, padding, padding)

            // 设置文本居中
            dynamicTextView.gravity = Gravity.CENTER

            // 添加阴影（仅在 API 21+ 上生效）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dynamicTextView.elevation = 4f  // 单位是 dp
            }


            dynamicTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            dynamicTextView.text = models[i].modelName + "(" + bytesToGigabytes(models[i].modelSize) + "GB)"
            dynamicTextView.id = View.generateViewId()  // 设置唯一的ID

            dynamicTextView.setOnClickListener { view ->
                // 创建一个Intent以启动新的Activity，这里假设是DetailActivity
                val intent = Intent(context, ChatActivity::class.java)

                // 你可以通过Intent传递数据，例如模型的ID或名称
                intent.putExtra("MODEL_NAME", models[i].modelName)
                // 由于我们在Fragment中，所以使用requireContext()获取上下文
                startActivity(intent)
            }

            dynamicTextView.setOnLongClickListener {
                    val popupMenu = PopupMenu(context, view)
                    popupMenu.menuInflater.inflate(R.menu.menu_options, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_delete -> {
                                val modelName = (dynamicTextView.text as String).substringBefore("(")
                                // 处理删除操作
                                Toast.makeText(context, "delete " + modelName, Toast.LENGTH_SHORT).show()
                                ModelOperation.deleteModel(modelName)
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()

                true
            }

            // 将动态创建的TextView添加到LinearLayout容器中
            binding.linearLayoutContainer.addView(dynamicTextView)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}