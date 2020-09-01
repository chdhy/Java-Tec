package com.hunter.apt

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements

abstract class BaseAnnotationProcessor : AbstractProcessor() {

    protected lateinit var messager: Messager
    protected lateinit var elements: Elements
    protected lateinit var filer: Filer

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        elements = processingEnv.elementUtils
        filer = processingEnv.filer
    }

}