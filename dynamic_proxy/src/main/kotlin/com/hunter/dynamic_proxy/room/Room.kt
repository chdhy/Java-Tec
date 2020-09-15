package com.hunter.dynamic_proxy.room

import com.hunter.dynamic_proxy.room.annotation.Insert
import com.hunter.dynamic_proxy.room.annotation.Query
import java.lang.reflect.*
import java.sql.Connection
import java.sql.DriverManager

class Room {

    companion object {
        lateinit var connection: Connection

        init {
            connect()
            createTable()
        }

        private fun connect(): Connection {
            try {
                Class.forName("org.sqlite.JDBC")
                connection = DriverManager.getConnection("jdbc:sqlite:test.db")
                println("open database")
            } catch (e: Exception) {

            }
            return connection
        }

        private fun createTable() {
            val statement = connection.createStatement()
            val createTableSql = """|CREATE TABLE IF NOT EXISTS employee
                    |(id INT PRIMARY KEY NOT NULL,
                    |name TEXT NOT NULL,
                    |age INT NOT NULL,
                    |address CHAR(50),
                    |salary REAL)
                """.trimMargin()
            statement.executeUpdate(createTableSql)
            statement.closeOnCompletion()
        }


        fun <T> create(clazz: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), InvocationHandler { _, method, args ->
                when (val annotation = method.annotations[0]) {
                    is Query -> query(method, annotation)
                    is Insert -> insert(args)
                    else -> Unit
                }
            }) as T
        }

        private fun insert(args: Array<Any>) {
            val collectionArgs = mutableListOf<Any>()
            if (Iterable::class.java.isAssignableFrom(args[0].javaClass).not()) {
                collectionArgs.add(args[0])
            } else {
                @Suppress("UNCHECKED_CAST")
                collectionArgs.addAll(args[0] as Iterable<Any>)
            }

            val statement = connection.createStatement()
            val insertSqlStrList = getInsertSqlList(collectionArgs)
            insertSqlStrList.forEach { statement.executeUpdate(it) }
            statement.closeOnCompletion()
        }

        private fun getInsertSqlList(collectionArgs: MutableList<Any>): List<String> {
            val entityType = collectionArgs[0].javaClass
            return collectionArgs.map { arg ->
                val fieldValues = entityType.declaredFields.map {
                    val declaredField = entityType.getDeclaredField(it.name)
                    declaredField.isAccessible = true
                    val value = declaredField.get(arg)
                    if (it.type == String::class.java && value != null) {
                        "'$value'"
                    } else {
                        value
                    }
                }
                    .joinToString(", ")
                "INSERT OR REPLACE INTO ${entityType.simpleName.toLowerCase()} VALUES ($fieldValues)"
            }
        }

        private fun query(method: Method, annotation: Query): Any {
            val returnType = method.genericReturnType
            var rawType: Type? = null
            lateinit var actualType: Type
            actualType = if (returnType is Class<*>) {
                returnType
            } else {
                rawType = (returnType as ParameterizedType).rawType
                returnType.actualTypeArguments[0]
            }

            val declaredFields = Class.forName(actualType.typeName).declaredFields

            val queryStr = if (annotation.statement.isEmpty()) getDefaultQueryStr(actualType, declaredFields)
            else annotation.statement

            val queryResult = getQueryResult(queryStr, declaredFields, actualType)

            return if (rawType == null /*单参数，非集合参数*/) queryResult[0] else queryResult
        }

        private fun getQueryResult(queryStr: String, declaredFields: Array<Field>, actualType: Type): MutableList<Any> {
            val result = mutableListOf<Any>()
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(queryStr)
            while (resultSet.next()) {
                val fieldValues = declaredFields.map {
                    when (it.type) {
                        String::class.java -> resultSet.getString(it.name)
                        Int::class.java, Integer::class.java -> resultSet.getInt(it.name)
                        Float::class.java, java.lang.Float::class.java -> resultSet.getFloat(it.name)
                        Long::class.java, java.lang.Long::class.java -> resultSet.getLong(it.name)
                        else -> Unit
                    }
                }
                val constructor = Class.forName(actualType.typeName)
                    .getDeclaredConstructor(*declaredFields.map { it.type }.toTypedArray())
                val element = constructor.newInstance(*fieldValues.toTypedArray())
                result.add(element)
            }
            statement.closeOnCompletion()
            return result
        }

        private fun getDefaultQueryStr(actualType: Type, declaredFields: Array<Field>): String {
            val fieldName = declaredFields.joinToString { it.name }
            val simpleTypeName = actualType.typeName.split(".").last()
            return "SELECT $fieldName FROM $simpleTypeName"
        }
    }

}

fun main() {
    val employeeDao = Room.create(EmployeeDao::class.java)
}