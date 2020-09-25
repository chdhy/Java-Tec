package com.hunter.lib

class ButterKnife {

    companion object {
        fun inject(target: Any) {
            val className = "${target::class.java.canonicalName}_ButterKnife"
            val clazz = Class.forName(className)
            val obj = clazz.newInstance()

            val bindMethod = clazz.getMethod("bind", target::class.java)
            bindMethod.invoke(obj, target)
        }
    }

}