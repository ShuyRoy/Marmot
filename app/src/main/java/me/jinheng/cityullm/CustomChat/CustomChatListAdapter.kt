package me.jinheng.cityullm.CustomChat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import me.jinheng.cityullm.R

class CustomChatListAdapter(context: Context) : BaseAdapter() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int {
            return CustomApi.chatItems!!.size
        }

        override fun getItem(i: Int): Any {
            return CustomApi.chatItems!![i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }


    override fun getView(position: Int, contentView: View?, viewGroup: ViewGroup?): View? {
            var contentView = contentView
            val holder: ViewHolder
            val currentItem = CustomApi.chatItems!![position]
            if (contentView == null) {
                if (getItemViewType(position) == 0) {
                    holder = ViewHolder()
                    contentView = inflater.inflate(R.layout.custom_chatview_bot, null)
                    holder.text = contentView.findViewById(R.id.text_bot_in)
                } else {
                    holder = ViewHolder()
                    contentView = inflater.inflate(R.layout.custom_chatview_user, null)
                    holder.text = contentView.findViewById(R.id.text_user_in)
                }
                contentView.tag = holder
            } else {
                holder = contentView.tag as ViewHolder
            }
            holder.text!!.text = currentItem.text.trim { it <= ' ' }
            return contentView
        }

        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun getItemViewType(position: Int): Int {
            val bean = CustomApi.chatItems!![position]
            return bean.type
        }

        class ViewHolder {
            var text: TextView? = null
        }
}