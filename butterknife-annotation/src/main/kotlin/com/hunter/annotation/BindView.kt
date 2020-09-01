package com.hunter.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class BindView(
    val resId: Int = 0
)