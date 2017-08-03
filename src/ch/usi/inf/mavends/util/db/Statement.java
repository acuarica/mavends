package ch.usi.inf.mavends.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PreparedStatement wrapper to ease passing arguments on each execution.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Statement implements AutoCloseable {

	/**
	 * The PreparedStatement to wrap.
	 */
	private final PreparedStatement stmt;

	/**
	 * Creates a new Statement wrapper.
	 * 
	 * @param conn
	 *            The connection on which create the statement.
	 * @param sql
	 *            The SQL statement that may contain one or more '?' IN
	 *            parameter placeholders.
	 * @throws SQLException
	 *             If a database access error occurs or this constructor is
	 *             called on a closed connection.
	 */
	Statement(Connection conn, String sql) throws SQLException {
		stmt = conn.prepareStatement(sql);
	}

	/**
	 * Executes the SQL given in the constructor with new values for the
	 * parameters.
	 * 
	 * @param values
	 *            The values that replace the '?' IN parameter placeholders.
	 * @throws SQLException
	 *             If a database access error occurs or this method is called on
	 *             a closed Statement.
	 */
	public void execute(Object... values) throws SQLException {
		for (int i = 0; i < values.length; i++) {
			stmt.setObject(i + 1, values[i]);
		}

		stmt.executeUpdate();
	}

	/**
	 * Retrieves the ROWID inserted of the last INSERT statement executed.
	 * 
	 * @return The last ROWID inserted.
	 * @throws SQLException
	 *             If a database access error occurs or this method is called on
	 *             a closed Statement.
	 */
	public long lastInsertRowid() throws SQLException {
		try (final ResultSet rs = stmt.getGeneratedKeys()) {
			return rs.getLong(1);
		}
	}

	@Override
	public void close() throws SQLException {
		stmt.close();
	}
}
