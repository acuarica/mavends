package ch.usi.inf.mavends.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Prepared statement wrapper.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Inserter implements AutoCloseable {

	private final PreparedStatement stmt;

	/**
	 * 
	 * @param conn
	 * @param sql
	 * @throws SQLException
	 */
	Inserter(Connection conn, String sql) throws SQLException {
		stmt = conn.prepareStatement(sql);
	}

	/**
	 * 
	 * @param values
	 * @throws SQLException
	 */
	public void insert(Object... values) throws SQLException {
		for (int i = 0; i < values.length; i++) {
			stmt.setObject(i + 1, values[i]);
		}

		stmt.executeUpdate();
	}

	@Override
	public void close() throws SQLException {
		stmt.close();
	}
}
