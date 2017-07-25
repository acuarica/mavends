package ch.usi.inf.mavends.util.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sqlite.SQLiteConnection;

/**
 * JDBC SQLite connection wrapper.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Db implements AutoCloseable {

	/**
	 * The SQLiteConnection to wrap.
	 */
	private final SQLiteConnection conn;

	/**
	 * The path of the database connection.
	 */
	public final String databasePath;

	/**
	 * Creates a new connection to the specified database path.
	 * 
	 * @param dbPath
	 *            The path of the database to connect. This should be a local
	 *            file path.
	 * @throws SQLException
	 */
	public Db(String databasePath) throws SQLException {
		this.databasePath = databasePath;

		conn = new SQLiteConnection(null, databasePath);

		pragma("journal_mode", "off");
		pragma("synchronous", "off");
		pragma("locking_mode", "exclusive");

		conn.setAutoCommit(false);
	}

	/**
	 * Executes the given SQL statement to under the current connection.
	 * 
	 * @param sql
	 *            The SQL statement to execute.
	 * @throws SQLException
	 */
	public void execute(String sql) throws SQLException {
		try (final java.sql.Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * Executes a SELECT statement under the current connection.
	 * 
	 * @param sql
	 *            The SELECT statement to execute.
	 * @param values
	 *            Additional arguments
	 * @return
	 * @throws SQLException
	 */
	public ResultSet select(String sql, Object... values) throws SQLException {
		final PreparedStatement stmt = conn.prepareStatement(sql);

		for (int i = 0; i < values.length; i++) {
			stmt.setObject(i + 1, values[i]);
		}

		final ResultSet rs = stmt.executeQuery();

		return rs;
	}

	/**
	 *
	 * @return
	 * @throws SQLException
	 */
	public long lastInsertRowId() throws SQLException {
		final ResultSet rs = select("select last_insert_rowid()");
		final long rowId = rs.getLong(1);

		return rowId;
	}

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public Statement createStatement(String sql) throws SQLException {
		return new Statement(conn, sql);
	}

	/**
	 * Commits the current transaction.
	 * 
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		conn.commit();
	}

	public void attach(String dbPath, String dbName) throws SQLException {
		conn.setAutoCommit(true);
		execute(String.format("attach database '%s' as %s", dbPath, dbName));
		conn.setAutoCommit(false);
	}

	public void detach(String dbName) throws SQLException {
		conn.setAutoCommit(true);
		execute(String.format("detach database %s", dbName));
		conn.setAutoCommit(false);
	}

	@Override
	public void close() throws SQLException {
		conn.close();
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @throws SQLException
	 */
	private void pragma(String name, Object value) throws SQLException {
		final String sql = String.format("pragma %s=%s", name, value);
		execute(sql);
	}
}
