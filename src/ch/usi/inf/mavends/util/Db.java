package ch.usi.inf.mavends.util;

import java.io.IOException;
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
public class Db {

	private static final Log log = new Log(System.out);

	/**
	 * 
	 */
	public final Connection conn;

	/**
	 * 
	 * @param dbPath
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Db(String dbPath) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
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

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public ResultSet select(String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		return rs;
	}

	/**
	 * 
	 * @param path
	 * @param message
	 * @throws IOException
	 * @throws SQLException
	 */
	public void send(String path, String message) throws IOException,
			SQLException {
		String sql = Resource.get(path);
		log.info(message + ": " + sql);
		execute(sql);
	}

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public Inserter createInserter(String sql) throws SQLException {
		return new Inserter(conn, sql);
	}
}
