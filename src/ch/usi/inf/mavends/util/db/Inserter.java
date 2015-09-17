package ch.usi.inf.mavends.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Prepared statement wrapper.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Inserter implements AutoCloseable {

	public final PreparedStatement stmt;

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

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public long lastInsertRowid() throws SQLException {
		final ResultSet rs = stmt.getGeneratedKeys();
		return rs.getLong(1);
	}

	@Override
	public void close() throws SQLException {
		stmt.close();
	}
}
