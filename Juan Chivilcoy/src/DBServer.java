import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DBServer {

	public static void main (String args[]) {
		Connection conn;
		Statement st;
		String sql;
		ResultSet res;
		
		try {
			// MariaDB
			//conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/ssdd2020?user=ssdd&password=ssdd");
			
			// MySQL
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3316/ssdd2020?user=ssdd&password=ssdd");
			conn.setAutoCommit(false);
			st = conn.createStatement();
			
			sql = "select * from Test where status = 1 limit 5 for update skip locked;";
			res = st.executeQuery(sql);
			while (true) {
				while (res.next()) {
					int id = res.getInt("id");
					String inc = res.getString("incoming");
					String out = res.getString("outgoing");
					int status = res.getInt("status");
					System.out.println("Procesando " + id + " " + inc + " " + out + " " + status);
					Thread t = new Thread(new DBWorker(conn, id, inc));
					//t.start();
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Done");
				res = st.executeQuery(sql);
			}

				// Barrera
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			//Scanner sc = new Scanner(System.in);
			//sc.nextLine();
			
			
	}
}
