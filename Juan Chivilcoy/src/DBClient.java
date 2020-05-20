import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBClient implements Runnable {

	@Override
	public void run() {
		Connection conn;
		Statement st;
		String sql;
		
		try {
			//MariaDB
			conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/ssdd2020?user=ssdd&password=ssdd");

			// MySQL
			//conn = DriverManager.getConnection("jdbc:mysql://localhost:3316/ssdd2020?user=ssdd&password=ssdd");
			st = conn.createStatement();
			
			sql = "insert into Test values (0, 'incoming', '', 1)";
			st.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main (String args[]) {
		Thread t;
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for (int i = 0; i < 10; i++) {
			t = new Thread(new DBClient());
			threads.add(t);
			t.start();
		}
		
		// Barrera
		for (int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Connection conn;
		Statement st;
		String sql;
		ResultSet res;
		
		try {
			// MariaDB
			conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/ssdd2020?user=ssdd&password=ssdd");
			
			// MySQL
			//conn = DriverManager.getConnection("jdbc:mysql://localhost:3316/ssdd2020?user=ssdd&password=ssdd");
			st = conn.createStatement();
			
			sql = "select * from Test;";
			res = st.executeQuery(sql);
			while (res.next()) {
				int id = res.getInt("id");
				String inc = res.getString("incoming");
				String out = res.getString("outgoing");
				int status = res.getInt("status");
				System.out.println("res: " + id + " " + inc + " " + out + " " + status);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
	}
}
