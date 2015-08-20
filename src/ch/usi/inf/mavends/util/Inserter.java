package ch.usi.inf.mavends.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Inserter {

	private String sql;
	private PreparedStatement stmt;

	Inserter(Connection conn, String sql) throws SQLException {
		this.sql = sql;
		stmt = conn.prepareStatement(sql);
	}

	public void insert(Object... values) {
		try {

			for (int i = 0; i < values.length; i++) {
				stmt.setObject(i + 1, values[i]);
			}

			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println(sql);
			for (int i = 0; i < values.length; i++) {
				System.err.println(values[i]);
			}

			throw new RuntimeException(e);
		}
	}
}
