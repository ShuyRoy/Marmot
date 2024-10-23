package me.jinheng.cityullm.newui

class ChatItem {
    var type: Int = 0
    var text: String = ""

    fun appendText(text: String) {
        this.text += text
    }
}