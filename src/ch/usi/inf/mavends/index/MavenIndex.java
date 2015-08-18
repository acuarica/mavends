package ch.usi.inf.mavends.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class MavenIndex {

	/**
	 * 
	 */
	public final Connection conn;

	/**
	 * 
	 * @param mavenIndexPath
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public MavenIndex(String mavenIndexPath) throws ClassNotFoundException,
			SQLException {
		Class.forName("org.sqlite.JDBC");

		conn = DriverManager.getConnection("jdbc:sqlite:" + mavenIndexPath);
	}

	/**
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public void execute(String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();
	}

	public ResultSet select(String sql) throws SQLException {
		Statement stmt = conn.createStatement();

		System.err.println(sql);
		
		ResultSet rs = stmt.executeQuery(sql);
		return rs;
	}
}
