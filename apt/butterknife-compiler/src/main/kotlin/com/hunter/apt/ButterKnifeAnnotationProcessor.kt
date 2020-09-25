package com.hunter.apt

import com.google.auto.service.AutoService
import com.hunter.annotation.BindView
import com.hunter.annotation.OnClick
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

const val bindView = "BindView"
const val onclick = "OnClick"

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(bindView, onclick)
class ButterKnifeAnnotationProcessor : BaseAnnotationProcessor() {

    private val bindViewMap: MutableMap<TypeElement, List<Element>> = mutableMapOf()
    private val onClickMap: MutableMap<TypeElement, List<Element>> = mutableMapOf()
    private val enclosingElements: MutableSet<TypeElement> = mutableSetOf()

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isNullOrEmpty()) return false // 注解处理器会处理多轮，但后续的处理可能没有实际的注解需要处理
        messager.printMessage(Diagnostic.Kind.NOTE, "real processing-----------------------")

        enclosingElements.clear()
        analyzeAnnotation(roundEnv) // 分析需要处理的注解
        enclosingElements.forEach {
            createAnnotationFile(it) // 以注解所在的类为单位生成新的文件
        }

        return false
    }

    private fun analyzeAnnotation(roundEnv: RoundEnvironment) {
        val bindView = roundEnv.getElementsAnnotatedWith(BindView::class.java)
        val groupedBindView = bindView.groupBy { it.enclosingElement as TypeElement }
        groupedBindView.forEach { bindViewMap[it.key] = it.value }
        enclosingElements.addAll(groupedBindView.keys)

        val onClick = roundEnv.getElementsAnnotatedWith(OnClick::class.java)
        val groupedOnClick = onClick.groupBy { it.enclosingElement as TypeElement }
        groupedOnClick.forEach { onClickMap[it.key] = it.value }
        enclosingElements.addAll(groupedOnClick.keys)
    }

    private fun createAnnotationFile(element: TypeElement) {
        val sourceBo = createSourceBO(element) // 获取创建文件需要用到的填充信息

        // write file
        val kotlinFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, sourceBo.packageStr, sourceBo.fileName)
        val kotlinFileWriter = kotlinFile.openWriter()
        kotlinFileWriter.write(fillFileTemplate(sourceBo))
        kotlinFileWriter.close()
    }

    private fun createSourceBO(element: TypeElement): SourceBO {
        val packageName = elements.getPackageOf(element).toString()
        val originalClassName = element.simpleName.toString()
        val targetClassSimpleName = element.simpleName.toString()
        val targetClassQualifiedName = element.qualifiedName.toString()
        val newClassName = "${originalClassName}_ButterKnife"
        val fileName = "${newClassName}.kt"
        val targetVar = "target"

        val importCode = bindViewMap[element]?.toSet()?.joinToString(separator = "\n") {
            "import ${it.asType()}"
        } ?: ""

        val bindViewCode = bindViewMap[element]?.joinToString(separator = "\n       ") {
            val resId = it.getAnnotation(BindView::class.java).resId
            "target.${it.simpleName} = ${it.asType().toString().split(".").last()}($resId)"
        } ?: ""

        val onClickCode = onClickMap[element]?.joinToString(separator = "\n       ") {
            val name = it.getAnnotation(OnClick::class.java).name
            "$targetVar.$name.setOnclickListener{ $targetVar.${it.simpleName}() }"
        } ?: ""

        return SourceBO(
            fileName, packageName, importCode, newClassName, targetClassQualifiedName,
            targetClassSimpleName, targetVar, bindViewCode, onClickCode
        )
    }

    // 写文件的内容，将 sourceBO 填充到模板里
    private fun fillFileTemplate(sourceBo: SourceBO): String {
        return """|package ${sourceBo.packageStr}
                    |
                    |import ${sourceBo.targetClassQualifiedName}
                    |${sourceBo.importCode}
                    |
                    |class ${sourceBo.className} {
                    |   
                    |   lateinit var ${sourceBo.targetVar}: ${sourceBo.targetClassSimpleName}
                    |   
                    |   fun bind(${sourceBo.targetVar}: ${sourceBo.targetClassSimpleName}){
                    |       this.${sourceBo.targetVar} = ${sourceBo.targetVar}
                    |       bindView()
                    |       onClick()
                    |   }
                    |
                    |   fun bindView() {
                    |       ${sourceBo.bindViewCode}
                    |   }
                    |   
                    |   fun onClick(){
                    |       ${sourceBo.onClickCode}
                    |   }
                    |
                    |}"""
            .trimMargin()
    }

    data class SourceBO(
        val fileName: String,
        val packageStr: String,
        val importCode: String,
        val className: String,
        val targetClassQualifiedName: String,
        val targetClassSimpleName: String,
        val targetVar: String,
        val bindViewCode: String,
        val onClickCode: String
    )

}