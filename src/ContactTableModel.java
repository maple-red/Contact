import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.stream.Collectors;

public class ContactTableModel extends AbstractTableModel {
    private final String[] COLUMNS = {"姓名", "分组", "电话", "邮箱", "扩展字段"};
    private List<Contact> contacts;

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return contacts == null ? 0 : contacts.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Contact contact = contacts.get(row);
        return switch (col) {
            case 0 -> contact.getName();
            case 1 -> contact.getGroup();
            case 2 -> formatPhones(contact);
            case 3 -> formatEmails(contact);
            case 4 -> formatCustomFields(contact);
            default -> "";
        };
    }

    private String formatPhones(Contact contact) {
        return contact.getPhones().stream()
                .map(phone -> phone.getType() + "：" + phone.getNumber())
                .collect(Collectors.joining(", "));
    }

    private String formatEmails(Contact contact) {
        return contact.getEmails().stream()
                .map(email -> email.getType() + "：" + email.getAddress())
                .collect(Collectors.joining(", "));
    }

    private String formatCustomFields(Contact contact) {
        return contact.getCustomFields().entrySet().stream()
                .map(entry -> entry.getKey() + "：" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    public Contact getContactAt(int row) {
        return contacts.get(row);
    }
}