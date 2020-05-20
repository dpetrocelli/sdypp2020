package org.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDBConnection {
	String host;
	String dbname;
	String url;
	String password;
	String username;
	Connection conn;
	Statement st;
	
	public MariaDBConnection (String host, String dbname, String username, String url, String password) {
		this.host = host;
		this.dbname = dbname;
		this.url = url;
		this.username = username;
		this.password = password;
		this.conn = null;
		this.st = null;
	}
	
	public void createConnection () {
		try {
			this.conn = DriverManager.getConnection(this.url, this.username, this.password);
			this.st = conn.createStatement();

			// validate if the structure is created
			try {
				// TABLA JOBS
				String createJobTable = "create table client (id int not null, `user` varchar(20) NOT NULL, `password` varchar(20) NOT NULL,`balance` DOUBLE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
				this.st.executeQuery(createJobTable);
				createJobTable = "ALTER TABLE `client` ADD PRIMARY KEY (`id`);";
				this.st.executeQuery(createJobTable);
				createJobTable = "ALTER TABLE client CHANGE id id INT(10)AUTO_INCREMENT;";
				this.st.executeQuery(createJobTable);
				/*String sql = "insert into client (user, password, balance) values ('david', 'david', 200.00);";
				this.st.executeQuery(sql);
				sql = "insert into client (user, password, balance) values ('nico', 'nico', 400.00);";
				this.st.executeQuery(sql);*/
			} catch (Exception e) {
				//System.err.println(" Estaba la tabla jobs");
            }
            
            try {
                // operations
                // IF 1 (+) - IF 2 (-)
				String createJobTable = "create table operations (id int not null, `user` int not null, `operationtype` varchar(30),`amount` DOUBLE, `date` DATE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
				this.st.executeQuery(createJobTable);
				createJobTable = "ALTER TABLE `operations` ADD PRIMARY KEY (`id`);";
				this.st.executeQuery(createJobTable);
				createJobTable = "ALTER TABLE operations CHANGE id id INT(10)AUTO_INCREMENT;";
				this.st.executeQuery(createJobTable);
			} catch (Exception e) {
				//System.err.println(" Estaba la tabla jobs");
			}

			try {
				// operations
				// IF 1 (+) - IF 2 (-)
				String createJobTable = "create table notifications (id int not null, `user` int not null, `operation` int not null,`amount` DOUBLE, `date` DATE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
				this.st.executeQuery(createJobTable);
				createJobTable = "ALTER TABLE `operations` ADD PRIMARY KEY (`id`);";
				this.st.executeQuery(createJobTable);
				createJobTable = "ALTER TABLE operations CHANGE id id INT(10)AUTO_INCREMENT;";
				this.st.executeQuery(createJobTable);
			} catch (Exception e) {
				//System.err.println(" Estaba la tabla jobs");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

}
