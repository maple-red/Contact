import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

public class ContactDialog extends JDialog {
    private final JTextField nameField = new JTextField(20);
    private final JTextField groupField = new JTextField(20);
    private final JList<String> phoneList = new JList<>();
    private final DefaultListModel<String> phoneListModel = new DefaultListModel<>();
    private final JList<String> emailList = new JList<>();
    private final DefaultListModel<String> emailListModel = new DefaultListModel<>();
    private final JTable customFieldTable;
    private final CustomFieldTableModel customFieldTableModel = new CustomFieldTableModel();

    private final JButton saveButton = new JButton("保存");
    private final JButton cancelButton = new JButton("取消");

    private Contact contact;
    private boolean saved = false;

    public ContactDialog(JFrame parent, Contact contact) {
        super(parent, contact == null ? "添加联系人" : "编辑联系人", true);
        this.contact = contact;
        phoneList.setModel(phoneListModel);
        emailList.setModel(emailListModel);
        customFieldTable = new JTable(customFieldTableModel);
        customFieldTable.setRowHeight(25);
        setLayout(new BorderLayout(10, 10));
        setSize(700, 500);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        infoPanel.add(new JLabel("姓名*:"));
        infoPanel.add(nameField);
        infoPanel.add(new JLabel("分组:"));
        infoPanel.add(groupField);

        JPanel phonePanel = new JPanel(new BorderLayout(5, 5));
        phonePanel.setBorder(BorderFactory.createTitledBorder("电话"));
        phonePanel.add(new JScrollPane(phoneList), BorderLayout.CENTER);

        JPanel phoneButtonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JButton addPhoneButton = new JButton("添加");
        JButton editPhoneButton = new JButton("编辑");
        JButton removePhoneButton = new JButton("删除");

        addPhoneButton.addActionListener(e -> addPhone());
        editPhoneButton.addActionListener(e -> editPhone());
        removePhoneButton.addActionListener(e -> removePhone());

        phoneButtonPanel.add(addPhoneButton);
        phoneButtonPanel.add(editPhoneButton);
        phoneButtonPanel.add(removePhoneButton);
        phonePanel.add(phoneButtonPanel, BorderLayout.SOUTH);

        JPanel emailPanel = new JPanel(new BorderLayout(5, 5));
        emailPanel.setBorder(BorderFactory.createTitledBorder("邮箱"));
        emailPanel.add(new JScrollPane(emailList), BorderLayout.CENTER);

        JPanel emailButtonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JButton addEmailButton = new JButton("添加");
        JButton editEmailButton = new JButton("编辑");
        JButton removeEmailButton = new JButton("删除");

        addEmailButton.addActionListener(e -> addEmail());
        editEmailButton.addActionListener(e -> editEmail());
        removeEmailButton.addActionListener(e -> removeEmail());

        emailButtonPanel.add(addEmailButton);
        emailButtonPanel.add(editEmailButton);
        emailButtonPanel.add(removeEmailButton);
        emailPanel.add(emailButtonPanel, BorderLayout.SOUTH);

        JPanel customFieldPanel = new JPanel(new BorderLayout(5, 5));
        customFieldPanel.setBorder(BorderFactory.createTitledBorder("自定义字段"));
        customFieldPanel.add(new JScrollPane(customFieldTable), BorderLayout.CENTER);

        JPanel customButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton addFieldButton = new JButton("添加");
        JButton removeFieldButton = new JButton("删除");

        addFieldButton.addActionListener(e -> addCustomField());
        removeFieldButton.addActionListener(e -> removeCustomField());

        customButtonPanel.add(addFieldButton);
        customButtonPanel.add(removeFieldButton);
        customFieldPanel.add(customButtonPanel, BorderLayout.SOUTH);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(infoPanel, BorderLayout.NORTH);
        leftPanel.add(phonePanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(emailPanel, BorderLayout.NORTH);
        rightPanel.add(customFieldPanel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        saveButton.addActionListener(this::saveContact);
        cancelButton.addActionListener(e -> dispose());

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        if (contact != null) {
            nameField.setText(contact.getName());
            groupField.setText(contact.getGroup() != null ? contact.getGroup() : "");

            for (Contact.Phone phone : contact.getPhones()) {
                phoneListModel.addElement(phone.toString());
            }

            for (Contact.Email email : contact.getEmails()) {
                emailListModel.addElement(email.toString());
            }

            for (Map.Entry<String, String> entry : contact.getCustomFields().entrySet()) {
                customFieldTableModel.addField(entry.getKey(), entry.getValue());
            }
        }

        setVisible(true);
    }

    private void addPhone() {

        String defaultType = "手机" + (phoneListModel.size() + 1);

        JTextField typeField = new JTextField(defaultType, 10);
        JTextField numberField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("类型:"));
        panel.add(typeField);
        panel.add(new JLabel("号码:"));
        panel.add(numberField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "添加电话",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String type = typeField.getText().trim();
            String number = numberField.getText().trim();
            if (!type.isEmpty() && !number.isEmpty()) {
                phoneListModel.addElement(type + "：" + number);
            }
        }
    }

    private void editPhone() {
        int index = phoneList.getSelectedIndex();
        if (index == -1) return;

        String current = phoneListModel.getElementAt(index);
        String[] parts = current.split("：", 2);
        String type = parts.length > 0 ? parts[0] : "手机";
        String number = parts.length > 1 ? parts[1] : "";

        JTextField typeField = new JTextField(type, 10);
        JTextField numberField = new JTextField(number, 15);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("类型:"));
        panel.add(typeField);
        panel.add(new JLabel("号码:"));
        panel.add(numberField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "编辑电话",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            type = typeField.getText().trim();
            number = numberField.getText().trim();
            if (!type.isEmpty() && !number.isEmpty()) {
                phoneListModel.set(index, type + "：" + number);
            }
        }
    }

    private void removePhone() {
        int index = phoneList.getSelectedIndex();
        if (index != -1) {
            phoneListModel.remove(index);
        }
    }

    private void addEmail() {
        String defaultType = "邮箱" + (emailListModel.size() + 1);

        JTextField typeField = new JTextField(defaultType, 10);
        JTextField addressField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("类型:"));
        panel.add(typeField);
        panel.add(new JLabel("地址:"));
        panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "添加邮箱",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String type = typeField.getText().trim();
            String address = addressField.getText().trim();
            if (!type.isEmpty() && !address.isEmpty()) {
                emailListModel.addElement(type + "：" + address);
            }
        }
    }

    private void editEmail() {
        int index = emailList.getSelectedIndex();
        if (index == -1) return;

        String current = emailListModel.getElementAt(index);
        String[] parts = current.split("：", 2);
        String type = parts.length > 0 ? parts[0] : "邮箱";
        String address = parts.length > 1 ? parts[1] : "";

        JTextField typeField = new JTextField(type, 10);
        JTextField addressField = new JTextField(address, 15);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("类型:"));
        panel.add(typeField);
        panel.add(new JLabel("地址:"));
        panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "编辑邮箱",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            type = typeField.getText().trim();
            address = addressField.getText().trim();
            if (!type.isEmpty() && !address.isEmpty()) {
                emailListModel.set(index, type + "：" + address);
            }
        }
    }

    private void removeEmail() {
        int index = emailList.getSelectedIndex();
        if (index != -1) {
            emailListModel.remove(index);
        }
    }

    private void addCustomField() {
        JTextField keyField = new JTextField(15);
        JTextField valueField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("字段名:"));
        panel.add(keyField);
        panel.add(new JLabel("字段值:"));
        panel.add(valueField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "添加自定义字段",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String key = keyField.getText().trim();
            String value = valueField.getText().trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                customFieldTableModel.addField(key, value);
            }
        }
    }

    private void removeCustomField() {
        int row = customFieldTable.getSelectedRow();
        if (row != -1) {
            customFieldTableModel.removeField(row);
        }
    }

    private void saveContact(ActionEvent e) {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "姓名不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Contact original = contact;
        Contact newContact = new Contact();
        newContact.setName(nameField.getText().trim());
        newContact.setGroup(groupField.getText().trim());

        List<Contact.Phone> phones = new ArrayList<>();
        for (int i = 0; i < phoneListModel.size(); i++) {
            String phoneStr = phoneListModel.getElementAt(i);
            String[] parts = phoneStr.split("：", 2);
            String type = parts.length > 0 ? parts[0] : "手机";
            String number = parts.length > 1 ? parts[1] : "";
            if (!number.isEmpty()) {
                phones.add(new Contact.Phone(type, number));
            }
        }
        newContact.setPhones(phones);

        List<Contact.Email> emails = new ArrayList<>();
        for (int i = 0; i < emailListModel.size(); i++) {
            String emailStr = emailListModel.getElementAt(i);
            String[] parts = emailStr.split("：", 2);
            String type = parts.length > 0 ? parts[0] : "邮箱";
            String address = parts.length > 1 ? parts[1] : "";
            if (!address.isEmpty()) {
                emails.add(new Contact.Email(type, address));
            }
        }
        newContact.setEmails(emails);

        Map<String, String> customFields = new HashMap<>();
        for (int i = 0; i < customFieldTableModel.getRowCount(); i++) {
            String key = (String) customFieldTableModel.getValueAt(i, 0);
            String value = (String) customFieldTableModel.getValueAt(i, 1);
            if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
                customFields.put(key, value);
            }
        }
        newContact.setCustomFields(customFields);

        if (original == null) {

            if (ContactDAO.addContact(newContact)) {
                saved = true;
                dispose();
            }
        } else {

            if (ContactDAO.updateContact(original, newContact)) {
                saved = true;
                dispose();
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Contact getContact() {
        return contact;
    }

    private static class CustomFieldTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"字段名", "字段值"};
        private final List<SimpleEntry<String, String>> fields = new ArrayList<>();

        @Override
        public int getRowCount() {
            return fields.size();
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
            SimpleEntry<String, String> field = fields.get(row);
            return col == 0 ? field.getKey() : field.getValue();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            SimpleEntry<String, String> field = fields.get(row);
            if (col == 0) {

                fields.set(row, new SimpleEntry<>((String) value, field.getValue()));
            } else {

                fields.set(row, new SimpleEntry<>(field.getKey(), (String) value));
            }
            fireTableCellUpdated(row, col);
        }

        public void addField(String key, String value) {
            fields.add(new SimpleEntry<>(key, value));
            fireTableRowsInserted(fields.size() - 1, fields.size() - 1);
        }

        public void removeField(int row) {
            fields.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public Map<String, String> getFields() {
            Map<String, String> result = new HashMap<>();
            for (SimpleEntry<String, String> entry : fields) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }
}