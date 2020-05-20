import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBServer {

	public static void main (String args[]) {
		Connection conn;
		Statement st;
		String sql;
		ResultSet res;
		
		// MariaDB
		try {
			// Maria
			//conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/ssdd2020?user=ssdd&password=ssdd");
			// MySQL
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3316/ssdd2020?user=ssdd&password=ssdd");

			st = conn.createStatement();
			conn.setAutoCommit(false);
			System.out.println(conn.getTransactionIsolation());
			
			sql = "select * from Test where status = 1 limit 5 for update skip locked;";
			res = st.executeQuery(sql);

			while (true) {
				while (res.next()) {
					// Thread que procese la tarea leida
					int id = res.getInt("id");
					Thread t = new Thread(new DBWorker(conn, id));
					//t.start();
					System.out.println("Procesando id: " + id);
				}
				Thread.sleep(6000);
				System.out.println("Loop");
				res = st.executeQuery(sql);
			}	
			
		} catch (SQLException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
