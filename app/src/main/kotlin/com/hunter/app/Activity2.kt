package com.hunter.app

import com.hunter.annotation.BindView
import com.hunter.lib.ButterKnife
import com.hunter.annotation.OnClick
import com.hunter.app.view.Button

class Activity2 {

    @BindView(2)
    lateinit var button: Button

    /**
     * foo方法是
     */
    @OnClick("button")
    fun foo() {
        println("Activity2 $button onClick")
    }

    fun onCreate() {
        ButterKnife.inject(this)
        println(button.toString())
        button.onClick?.invoke()
    }

}