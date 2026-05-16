import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class CSVImporter {
    private static final String CSV_FILE = "data.csv";

    public static void importData() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int count = 0;
                try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
                    String line;
                    boolean firstLine = true;
                    try (Connection conn = DBUtil.getConnection()) {

                        conn.setAutoCommit(false);

                        String contactSql = "INSERT INTO contacts (name, group_name) VALUES (?, ?)";
                        PreparedStatement contactStmt = conn.prepareStatement(contactSql);

                        String phoneSql = "INSERT INTO phones (contact_name, phone_type, phone_number) VALUES (?, ?, ?)";
                        PreparedStatement phoneStmt = conn.prepareStatement(phoneSql);

                        String emailSql = "INSERT INTO emails (contact_name, email_type, email_address) VALUES (?, ?, ?)";
                        PreparedStatement emailStmt = conn.prepareStatement(emailSql);

                        while ((line = br.readLine()) != null) {
                            if (firstLine) {
                                firstLine = false;
                                continue;
                            }

                            String[] parts = line.split(",");
                            if (parts.length < 5) {
                                System.err.println("跳过无效行: " + line);
                                continue;
                            }

                            String name = parts[0].trim();
                            String phone1 = parts[1].trim();
                            String phone2 = parts[2].trim();
                            String email = parts[3].trim();
                            String group = parts[4].trim();

                            contactStmt.setString(1, name);
                            contactStmt.setString(2, group);
                            contactStmt.executeUpdate();

                            insertPhone(phoneStmt, name, "手机1", phone1);
                            insertPhone(phoneStmt, name, "手机2", phone2);

                            if (!email.isEmpty()) {
                                emailStmt.setString(1, name);
                                emailStmt.setString(2, "邮箱");
                                emailStmt.setString(3, email);
                                emailStmt.addBatch();
                            }

                            count++;

                            if (count % 50 == 0) {
                                phoneStmt.executeBatch();
                                emailStmt.executeBatch();
                                conn.commit();
                            }
                        }

                        phoneStmt.executeBatch();
                        emailStmt.executeBatch();
                        conn.commit();

                        contactStmt.close();
                        phoneStmt.close();
                        emailStmt.close();

                        return count;
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException("导入失败: " + e.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    Integer count = get();
                    if (count != null) {
                        JOptionPane.showMessageDialog(
                                null,
                                "成功导入 " + count + " 条联系人记录",
                                "导入完成",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            null,
                            "导入失败: " + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private static void insertPhone(PreparedStatement stmt, String contactName, String type, String phone)
            throws SQLException {
        if (!phone.isEmpty()) {
            stmt.setString(1, contactName);
            stmt.setString(2, type);
            stmt.setString(3, phone);
            stmt.addBatch();
        }
    }
}