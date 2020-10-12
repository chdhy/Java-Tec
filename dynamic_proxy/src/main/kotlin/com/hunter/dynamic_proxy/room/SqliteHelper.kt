package com.hunter.dynamic_proxy.room

import java.sql.Connection
import java.sql.DriverManager

class SqliteHelper {

    companion object {

        lateinit var connection: Connection

        fun connect(): Connection {
            try {
                Class.forName("org.sqlite.JDBC")
                connection = DriverManager.getConnection("jdbc:sqlite:test.db")
                println("open database")
            } catch (e: Exception) {

            }
            return connection
        }

        fun createTable() {
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

        fun insert() {
            val statement = connection.createStatement()
            statement.executeUpdate("INSERT OR REPLACE INTO employee VALUES (1, 'Paul', 32, 'California', 20000.0 )")
            statement.executeUpdate("INSERT OR REPLACE INTO employee VALUES (2, 'Allen', 25, 'Texas', 15000.0)")
            statement.executeUpdate("INSERT OR REPLACE INTO employee VALUES (3, 'Teddy', 23, 'Norway', 20000.0 )")
            statement.executeUpdate("INSERT OR REPLACE INTO employee VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.0 )")
            statement.executeUpdate("INSERT OR REPLACE INTO employee VALUES (5, 'tom', 18, 'foo', 15000.0)")
            statement.executeUpdate("INSERT OR REPLACE INTO employee VALUES (6, 'tom', 19, 'bar', 25000.0)")
            "INSERT OR REPLACE INTO employee VALUES (5, 'tom', 18, 'foo', 15000.0)\n" +
                    "INSERT OR REPLACE INTO employee VALUES (6, 'tom', 19, 'bar', 25000.0)"
            statement.closeOnCompletion()
        }

        fun query(): List<Employee> {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM employee")
            val employees = mutableListOf<Employee>()
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val name = resultSet.getString("name")
                val age = resultSet.getInt("age")
                val address = resultSet.getString("address")
                val salary = resultSet.getFloat("salary")
                employees.add(Employee(id, name, age, address, salary))
            }
            resultSet.close()
            statement.closeOnCompletion()
            return employees
        }

    }

}

fun main() {
    val connect = SqliteHelper.connect()
    SqliteHelper.createTable()
    SqliteHelper.insert()
    val employees = SqliteHelper.query()
    println(employees.joinToString("\n"))
    connect.close()
}
