package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbUtil {
	
	static Connection conn = null;
	static Statement statment = null;
	static ResultSet ret = null;
	
	/*
	 * Connect to the oracle database
	*/
	public static Connection connectDB() throws SQLException {
		String DB_URL = Configuration.jdbcUrl;
		String USER = Configuration.username;
		String PASS = Configuration.password;
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		return conn;
	}
	
	
}
