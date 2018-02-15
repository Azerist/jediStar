package fr.jedistar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.json.JSONObject;

public abstract class StaticVars {

	public static JSONObject jsonSettings;
	public static JSONObject jsonMessages;

	private static Connection jdbcConnection;
	private static String jdbcUrl;
	private static String jdbcUser;
	private static String jdbcPasswd;
	
	public static boolean useCache = true;

	public static Connection getJdbcConnection() throws SQLException {

		if (jdbcConnection == null || !jdbcConnection.isValid(10)) {
			jdbcConnection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPasswd);
		}

		return jdbcConnection;
	}
	
	public static Connection getIndependantJdbcConnection() throws SQLException{
		return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPasswd);
	}

	public static void setJdbcConnection(Connection jdbcConnection) {
		StaticVars.jdbcConnection = jdbcConnection;
	}

	public static void setJdbcParams(String url, String user, String pwd) {
		jdbcUrl = url;
		jdbcPasswd = pwd;
		jdbcUser = user;
	}
	
	
}
