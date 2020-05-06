import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class DBCluster implements Runnable {

	@Override
	public void run() {
		Connection conn;
		Statement st;
		String sql;
		
		try {
			// MySQL -- si conecto a puerto RO, tira error al hacer Write
			conn = DriverManager.getConnection("jdbc:mysql:replication//localhost:3310,localhost:3320,localhost:3330/ssdd2020?user=ssdd&password=ssdd");
			//conn = DriverManager.getConnection("jdbc:mysql://localhost:3320/ssdd2020?user=ssdd&password=ssdd");
			st = conn.createStatement();
			
			sql = "insert into Test values (0, 'incoming', '', 1)";
			//st.executeUpdate(sql);
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main (String args[]) {
		Thread t;
		ArrayList<Thread> threads = new ArrayList<Thread>();

		for (int i = 0; i < 10; i++) {
			t = new Thread(new DBCluster());
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
		Properties props = new Properties();
		
		try {
			props.put("roundRobinLoadBalance", true);
			
			// MySQL
			//conn = DriverManager.getConnection("jdbc:mysql://localhost:3310/ssdd2020?user=ssdd&password=ssdd");
			conn = DriverManager.getConnection("jdbc:mysql:replication//localhost:3310,localhost:3320,localhost:3330/ssdd2020?user=ssdd&password=ssdd", props);
			conn.setReadOnly(true);
			st = conn.createStatement();
			sql = "select @@port";
			res = st.executeQuery(sql);
			if (res.next())
				System.out.println("Puerto:"  + res.getInt(1));
			System.out.println(conn.getTransactionIsolation());
			
			Thread.sleep(6000);
			sql = "select * from Test;";
			res = st.executeQuery(sql);
			while (res.next()) {
				int id = res.getInt("id");
				String inc = res.getString("incoming");
				String out = res.getString("outgoing");
				int status = res.getInt("status");
				System.out.println("res: " + id + " " + inc + " " + out + " " + status);
			}
		} catch (SQLException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
	}
}
