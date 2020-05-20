import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBWorker implements Runnable {
	private Connection conn;
	private int id;
	
	public DBWorker(Connection c, int id) {
		conn = c;
		this.id = id;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Statement st = null;
		String sql;
		
		try {
			// Marcarla bajo ejecucion
			sql = "update test set status = 2 where id = " + id;
			st = conn.createStatement();
			st.executeUpdate(sql);

			// Procesar la tarea
			
			// Marcarla como procesada
			sql = "update test set status = 3 where id = " + id;
			st.executeUpdate(sql);
			
			// Commit
			sql = "commit;";
			//st.executeUpdate(sql);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
