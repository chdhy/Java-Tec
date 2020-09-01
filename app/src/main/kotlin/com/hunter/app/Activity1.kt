package com.hunter.app

import com.hunter.annotation.BindView
import com.hunter.lib.ButterKnife
import com.hunter.annotation.OnClick
import com.hunter.app.view.TextView

class Activity1 {

    @BindView(1)
    lateinit var textView: TextView

    /**
     * foo方法是
     */
    @OnClick("textView")
    fun foo() {
        println("Activity1 $textView onClick")
    }

    fun onCreate() {
        ButterKnife.inject(this)
        println(textView.toString())
        textView.onClick?.invoke()
    }

}