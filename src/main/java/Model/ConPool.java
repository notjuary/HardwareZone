package Model;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class ConPool {
	private static DataSource datasource;

	public static Connection getConnection() throws SQLException {
		if (datasource == null) {
			PoolProperties p = new PoolProperties();
			String dbHost = System.getenv("MYSQL_HOST");
			if (dbHost == null) {
				dbHost = "localhost";  // o un valore di default
			}
			p.setUrl("jdbc:mysql://db:3306/ecommerce?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
			p.setDriverClassName("com.mysql.cj.jdbc.Driver");
			p.setUsername("root");
			p.setPassword("teograuso01");
			p.setMaxActive(100);
			p.setInitialSize(10);
			p.setMinIdle(10);
			p.setRemoveAbandonedTimeout(60);
			p.setRemoveAbandoned(true);
			datasource = new DataSource();
			datasource.setPoolProperties(p);
		}
		return datasource.getConnection();
	}
}