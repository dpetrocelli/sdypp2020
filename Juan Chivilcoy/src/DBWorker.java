import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBWorker implements Runnable {
	private Connection conn;
	private int id;
	private String inc;
	
	public DBWorker(Connection c, int i, String incoming) {
		id = i;
		inc = incoming;
		conn = c;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Statement st;
		try {
			st = conn.createStatement();
			String query;
			
			// Marcar bajo procesamiento
			// update test set status = 2 where id = ;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			query = "update test set status = 2 where id = " + id;
			st.executeUpdate(query);
			// Procesar
			
			// Finaliza
			// update test set outgoing = resultado where id = ;
			// update test set status = 3 where id = ;
			query = "commit";
			st.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
