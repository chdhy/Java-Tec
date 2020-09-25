package com.hunter.app.view

data class TextView(val resId: Int) {
    var onClick: (() -> Unit)? = null

    fun setOnclickListener(onClick: () -> Unit) {
        this.onClick = onClick
    }
}