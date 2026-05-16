import java.sql.*;
import javax.swing.JOptionPane;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/contact_db?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "20050810";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库驱动加载失败", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void executeSQL(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}