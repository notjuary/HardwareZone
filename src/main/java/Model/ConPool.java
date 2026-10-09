package Model;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class ConPool {
	private static DataSource datasource;
	private static DataSource testDataSource;

	public static void setTestDataSource(DataSource ds) {
		testDataSource = ds;
	}

	public static void clearTestDataSource() {
		testDataSource = null;
	}

	public static Connection getConnection() throws SQLException {
		if (testDataSource != null) {
			return testDataSource.getConnection();
		}

		if (datasource == null) {
			PoolProperties p = new PoolProperties();

			// Leggi tutte le credenziali da variabili d'ambiente
			String dbHost = System.getenv("MYSQL_HOST");
			String dbPort = System.getenv("MYSQL_PORT");
			String dbName = System.getenv("MYSQL_DATABASE");
			String dbUser = System.getenv("MYSQL_USER");
			String dbPassword = System.getenv("MYSQL_PASSWORD");

			// Fallback solo per valori NON sensibili (host, porta, nome db, utente)
			if (dbHost == null) dbHost = "localhost";
			if (dbPort == null) dbPort = "3306";
			if (dbName == null) dbName = "ecommerce";
			if (dbUser == null) dbUser = "root";

			// La password NON deve avere fallback: se manca, l'app non deve avviarsi
			if (dbPassword == null) {
				throw new IllegalStateException("La variabile d'ambiente MYSQL_PASSWORD non è impostata.");
			}

			String timezone = TimeZone.getDefault().getID();
			String url = String.format(
					"jdbc:mysql://%s:%s/%s"
							+ "?useSSL=false"
							+ "&allowPublicKeyRetrieval=true"
							+ "&serverTimezone=%s"
							+ "&useUnicode=true"
							+ "&characterEncoding=UTF-8",
					dbHost, dbPort, dbName, timezone
			);

			p.setUrl(url);
			p.setDriverClassName("com.mysql.cj.jdbc.Driver");
			p.setUsername(dbUser);
			p.setPassword(dbPassword);
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