package ch.usi.inf.mavends.util.db;

import java.sql.PreparedStatement;
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
	private final SQLiteConnection conn;

	/**
	 * 
	 * @param dbPath
	 * @throws SQLException
	 */
	public Db(String dbPath) throws SQLException {
		conn = new SQLiteConnection(null, dbPath);

		pragma("journal_mode", "off");
		pragma("synchronous", "off");
		pragma("locking_mode", "exclusive");

		conn.setAutoCommit(false);
	}

	private void pragma(String name, Object value) throws SQLException {
		execute(String.format("pragma %s=%s", name, value));
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
	 * @param values
	 * @return
	 * @throws SQLException
	 */
	public ResultSet select(String sql, Object... values) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql);

		for (int i = 0; i < values.length; i++) {
			stmt.setObject(i + 1, values[i]);
		}

		ResultSet rs = stmt.executeQuery();

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

	/**
	 * Commits the current transaction.
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		conn.commit();
	}
}
