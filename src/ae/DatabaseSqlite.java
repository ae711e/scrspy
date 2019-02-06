/*
 * Copyright (c) 2017. Aleksey Eremin
 * 28.01.17 21:26
 */

package ae;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by ae on 28.01.2017.
 * База данных Sqlite.
 */
public class DatabaseSqlite extends Database
{
    private String f_databaseName;  // имя файлы базы данных
    
    /**
     * Конструктор
     * в нем формируется имя базы данных
     */
    public DatabaseSqlite(String dbName)
    {
        f_databaseName = dbName;
    }
    
    /**
     * Возвращает соединение к базе данных SQlite
     * @return соединение к БД
     */
    @Override
    public synchronized Connection getDbConnection()
    {
        if(f_connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                f_connection = DriverManager.getConnection("jdbc:sqlite:" + f_databaseName);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return f_connection;
    }

} // end of class
