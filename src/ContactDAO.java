import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;

public class ContactDAO {

    public static List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();

        String contactSql = "SELECT * FROM contacts";
        String phoneSql = "SELECT phone_type, phone_number FROM phones WHERE contact_name = ?";
        String emailSql = "SELECT email_type, email_address FROM emails WHERE contact_name = ?";
        String fieldSql = "SELECT field_key, field_value FROM custom_fields WHERE contact_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement contactStmt = conn.prepareStatement(contactSql);
             ResultSet contactRs = contactStmt.executeQuery()) {

            while (contactRs.next()) {
                Contact contact = new Contact();
                contact.setName(contactRs.getString("name"));
                contact.setGroup(contactRs.getString("group_name"));

                try (PreparedStatement phoneStmt = conn.prepareStatement(phoneSql)) {
                    phoneStmt.setString(1, contact.getName());
                    try (ResultSet phoneRs = phoneStmt.executeQuery()) {
                        while (phoneRs.next()) {
                            contact.addPhone(new Contact.Phone(
                                    phoneRs.getString("phone_type"),
                                    phoneRs.getString("phone_number")
                            ));
                        }
                    }
                }

                try (PreparedStatement emailStmt = conn.prepareStatement(emailSql)) {
                    emailStmt.setString(1, contact.getName());
                    try (ResultSet emailRs = emailStmt.executeQuery()) {
                        while (emailRs.next()) {
                            contact.addEmail(new Contact.Email(
                                    emailRs.getString("email_type"),
                                    emailRs.getString("email_address")
                            ));
                        }
                    }
                }

                try (PreparedStatement fieldStmt = conn.prepareStatement(fieldSql)) {
                    fieldStmt.setString(1, contact.getName());
                    try (ResultSet fieldRs = fieldStmt.executeQuery()) {
                        while (fieldRs.next()) {
                            contact.addCustomField(
                                    fieldRs.getString("field_key"),
                                    fieldRs.getString("field_value")
                            );
                        }
                    }
                }

                contacts.add(contact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载联系人失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
        return contacts;
    }

    public static boolean addContact(Contact contact) {
        String contactSql = "INSERT INTO contacts (name, group_name) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement contactStmt = conn.prepareStatement(contactSql)) {

            contactStmt.setString(1, contact.getName());
            contactStmt.setString(2, contact.getGroup());
            contactStmt.executeUpdate();
            savePhones(conn, contact.getName(), contact.getPhones());

            saveEmails(conn, contact.getName(), contact.getEmails());

            saveCustomFields(conn, contact.getName(), contact.getCustomFields());

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "添加联系人失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public static boolean updateContact(Contact original, Contact updated) {
        String contactSql = "UPDATE contacts SET name = ?, group_name = ? WHERE name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement contactStmt = conn.prepareStatement(contactSql)) {

            conn.setAutoCommit(false);

            contactStmt.setString(1, updated.getName());
            contactStmt.setString(2, updated.getGroup());
            contactStmt.setString(3, original.getName());
            contactStmt.executeUpdate();

            deletePhones(conn, updated.getName());
            deleteEmails(conn, updated.getName());
            deleteCustomFields(conn, updated.getName());

            savePhones(conn, updated.getName(), updated.getPhones());
            saveEmails(conn, updated.getName(), updated.getEmails());
            saveCustomFields(conn, updated.getName(), updated.getCustomFields());

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "更新联系人失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public static boolean deleteContact(String name) {
        String sql = "DELETE FROM contacts WHERE name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "删除联系人失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public static List<Contact> searchContacts(String name, String phone) {
        List<Contact> contacts = new ArrayList<>();

        String sql = "SELECT DISTINCT c.name, c.group_name " +
                "FROM contacts c " +
                "LEFT JOIN phones p ON c.name = p.contact_name " +
                "WHERE (c.name LIKE ? OR ? IS NULL) " +
                "AND (p.phone_number LIKE ? OR ? IS NULL)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name == null || name.isEmpty() ? null : "%" + name + "%");
            pstmt.setString(2, name == null || name.isEmpty() ? null : "%" + name + "%");
            pstmt.setString(3, phone == null || phone.isEmpty() ? null : "%" + phone + "%");
            pstmt.setString(4, phone == null || phone.isEmpty() ? null : "%" + phone + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Contact contact = new Contact();
                    contact.setName(rs.getString("name"));
                    contact.setGroup(rs.getString("group_name"));

                    loadContactDetails(conn, contact);

                    contacts.add(contact);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "搜索失败: " + e.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
        }
        return contacts;
    }

    private static void loadContactDetails(Connection conn, Contact contact) throws SQLException {

        String phoneSql = "SELECT phone_type, phone_number FROM phones WHERE contact_name = ?";
        try (PreparedStatement phoneStmt = conn.prepareStatement(phoneSql)) {
            phoneStmt.setString(1, contact.getName());
            try (ResultSet phoneRs = phoneStmt.executeQuery()) {
                while (phoneRs.next()) {
                    contact.addPhone(new Contact.Phone(
                            phoneRs.getString("phone_type"),
                            phoneRs.getString("phone_number")
                    ));
                }
            }
        }

        String emailSql = "SELECT email_type, email_address FROM emails WHERE contact_name = ?";
        try (PreparedStatement emailStmt = conn.prepareStatement(emailSql)) {
            emailStmt.setString(1, contact.getName());
            try (ResultSet emailRs = emailStmt.executeQuery()) {
                while (emailRs.next()) {
                    contact.addEmail(new Contact.Email(
                            emailRs.getString("email_type"),
                            emailRs.getString("email_address")
                    ));
                }
            }
        }

        String fieldSql = "SELECT field_key, field_value FROM custom_fields WHERE contact_name = ?";
        try (PreparedStatement fieldStmt = conn.prepareStatement(fieldSql)) {
            fieldStmt.setString(1, contact.getName());
            try (ResultSet fieldRs = fieldStmt.executeQuery()) {
                while (fieldRs.next()) {
                    contact.addCustomField(
                            fieldRs.getString("field_key"),
                            fieldRs.getString("field_value")
                    );
                }
            }
        }
    }

    private static void savePhones(Connection conn, String contactName, List<Contact.Phone> phones)
            throws SQLException {
        if (phones == null || phones.isEmpty()) return;

        String sql = "INSERT INTO phones (contact_name, phone_type, phone_number) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Contact.Phone phone : phones) {
                pstmt.setString(1, contactName);
                pstmt.setString(2, phone.getType());
                pstmt.setString(3, phone.getNumber());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void saveEmails(Connection conn, String contactName, List<Contact.Email> emails)
            throws SQLException {
        if (emails == null || emails.isEmpty()) return;

        String sql = "INSERT INTO emails (contact_name, email_type, email_address) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Contact.Email email : emails) {
                pstmt.setString(1, contactName);
                pstmt.setString(2, email.getType());
                pstmt.setString(3, email.getAddress());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void saveCustomFields(Connection conn, String contactName, Map<String, String> fields)
            throws SQLException {
        if (fields == null || fields.isEmpty()) return;

        String sql = "INSERT INTO custom_fields (contact_name, field_key, field_value) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                pstmt.setString(1, contactName);
                pstmt.setString(2, entry.getKey());
                pstmt.setString(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void deletePhones(Connection conn, String contactName) throws SQLException {
        String sql = "DELETE FROM phones WHERE contact_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contactName);
            pstmt.executeUpdate();
        }
    }

    private static void deleteEmails(Connection conn, String contactName) throws SQLException {
        String sql = "DELETE FROM emails WHERE contact_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contactName);
            pstmt.executeUpdate();
        }
    }

    private static void deleteCustomFields(Connection conn, String contactName) throws SQLException {
        String sql = "DELETE FROM custom_fields WHERE contact_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contactName);
            pstmt.executeUpdate();
        }
    }

    public static void clearDatabase() {
        try (Connection conn = DBUtil.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("TRUNCATE TABLE contacts");
                stmt.execute("TRUNCATE TABLE phones");
                stmt.execute("TRUNCATE TABLE emails");
                stmt.execute("TRUNCATE TABLE custom_fields");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "清空数据库失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}