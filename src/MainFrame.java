import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

public class MainFrame extends JFrame {
    private final ContactTableModel tableModel = new ContactTableModel();
    private final JTable contactTable = new JTable(tableModel);
    private final JTextField searchNameField = new JTextField(15);
    private final JTextField searchPhoneField = new JTextField(15);
    private List<Contact> currentContacts;

    public MainFrame() {
        setTitle("名片管理系统");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadContacts();
    }

    private void initUI() {
        contactTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(contactTable);

        JButton addButton = new JButton("添加");
        JButton editButton = new JButton("修改");
        JButton deleteButton = new JButton("删除");
        JButton importButton = new JButton("导入CSV");
        JButton viewButton = new JButton("查看");

        addButton.addActionListener(e -> addContact());
        editButton.addActionListener(e -> editContact());
        deleteButton.addActionListener(e -> deleteContact());
        importButton.addActionListener(e -> importCSV());
        viewButton.addActionListener(e -> viewContact());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(importButton);

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchContacts());

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("姓名:"));
        searchPanel.add(searchNameField);
        searchPanel.add(new JLabel("电话:"));
        searchPanel.add(searchPhoneField);
        searchPanel.add(searchButton);

        JButton sortNameButton = new JButton("按姓名排序");
        JButton sortPhoneButton = new JButton("按手机号排序");

        sortNameButton.addActionListener(e -> sortByName());
        sortPhoneButton.addActionListener(e -> sortByPhone());

        JPanel sortPanel = new JPanel();
        sortPanel.add(sortNameButton);
        sortPanel.add(sortPhoneButton);

        setLayout(new BorderLayout(10, 10));
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(sortPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadContacts() {
        SwingUtilities.invokeLater(() -> {
            currentContacts = ContactDAO.getAllContacts();
            tableModel.setContacts(currentContacts);
        });
    }

    private void addContact() {
        ContactDialog dialog = new ContactDialog(this, null);
        if (dialog.isSaved()) {
            loadContacts();
        }
    }

    private void editContact() {
        int selectedRow = contactTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个联系人", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = contactTable.convertRowIndexToModel(selectedRow);
        Contact contact = tableModel.getContactAt(modelRow);

        ContactDialog dialog = new ContactDialog(this, contact);
        if (dialog.isSaved()) {
            loadContacts();
        }
    }

    private void viewContact() {
        int selectedRow = contactTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个联系人", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = contactTable.convertRowIndexToModel(selectedRow);
        Contact contact = tableModel.getContactAt(modelRow);

        new ContactViewDialog(this, contact).setVisible(true);
    }

    private void deleteContact() {
        int selectedRow = contactTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个联系人", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = contactTable.convertRowIndexToModel(selectedRow);
        Contact contact = tableModel.getContactAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要删除 " + contact.getName() + " 吗?",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (ContactDAO.deleteContact(contact.getName())) {
                loadContacts();
            }
        }
    }

    private void searchContacts() {
        String name = searchNameField.getText().trim();
        String phone = searchPhoneField.getText().trim();

        SwingUtilities.invokeLater(() -> {
            currentContacts = ContactDAO.searchContacts(
                    name.isEmpty() ? null : name,
                    phone.isEmpty() ? null : phone
            );
            tableModel.setContacts(currentContacts);
        });
    }

    private void importCSV() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要导入CSV数据吗？这将清空现有数据！",
                "确认导入",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ContactDAO.clearDatabase();

            CSVImporter.importData();

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    loadContacts();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void sortByName() {
        if (currentContacts == null || currentContacts.isEmpty()) return;

        currentContacts.sort(Comparator.comparing(Contact::getName));
        tableModel.setContacts(currentContacts);
    }

    private void sortByPhone() {
        if (currentContacts == null || currentContacts.isEmpty()) return;

        currentContacts.sort((c1, c2) -> {
            String p1 = c1.getPhones().isEmpty() ? "" : c1.getPhones().get(0).getNumber();
            String p2 = c2.getPhones().isEmpty() ? "" : c2.getPhones().get(0).getNumber();
            return p1.compareTo(p2);
        });

        tableModel.setContacts(currentContacts);
    }
}