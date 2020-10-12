package com.hunter.dynamic_proxy.room

import com.hunter.dynamic_proxy.room.annotation.*
import java.lang.reflect.ParameterizedType

@Dao
interface EmployeeDao {

    @Insert
    fun create(employees: List<Employee>)

//    @Update
//    fun update(employees: List<Employee>): Int

    @Query
    fun getAllEmployees(): List<Employee>

//    @Query("SELECT * FROM employee WHERE age = :age")
//    fun getEmployees(age: Int): List<Employee>
//
//    @Delete
//    fun delete(employees: List<Employee>): Int

}

fun main() {
    val returnType = (EmployeeDao::class.java.getMethod("getAllEmployees").annotations)
    println(returnType[0])
}