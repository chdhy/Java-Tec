package com.hunter.app.view

data class Button(val resId: Int) {
    var onClick: (() -> Unit)? = null

    fun setOnclickListener(onClick: () -> Unit) {
        this.onClick = onClick
    }
}