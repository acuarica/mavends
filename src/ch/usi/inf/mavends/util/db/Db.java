package ch.usi.inf.mavends.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConnection;

/**
 * JDBC SQLite connection wrapper.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Db implements AutoCloseable {

	/**
	 * 
	 */
	public final SQLiteConnection conn;

	/**
	 * 
	 * @param dbPath
	 * @throws SQLException
	 */
	public Db(String dbPath) throws SQLException {
		conn = new SQLiteConnection(null, dbPath);
		execute("pragma journal_mode=off");
		conn.setAutoCommit(false);
	}

	/**
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public void execute(String sql) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		}
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
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public Inserter createInserter(String sql) throws SQLException {
		return new Inserter(conn, sql);
	}

	@Override
	public void close() throws SQLException {
		conn.close();
	}
}
