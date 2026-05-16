import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ContactViewDialog extends JDialog {
    public ContactViewDialog(JFrame parent, Contact contact) {
        super(parent, "查看联系人", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(createInfoField("姓名:", contact.getName()));
        mainPanel.add(createInfoField("分组:", contact.getGroup()));

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(new JSeparator());
        mainPanel.add(Box.createVerticalStrut(10));

        if (!contact.getPhones().isEmpty()) {
            mainPanel.add(new JLabel("电话:"));
            for (Contact.Phone phone : contact.getPhones()) {
                mainPanel.add(createInfoField(phone.getType() + ":", phone.getNumber()));
            }
            mainPanel.add(Box.createVerticalStrut(10));
        }

        if (!contact.getEmails().isEmpty()) {
            mainPanel.add(new JLabel("邮箱:"));
            for (Contact.Email email : contact.getEmails()) {
                mainPanel.add(createInfoField(email.getType() + ":", email.getAddress()));
            }
            mainPanel.add(Box.createVerticalStrut(10));
        }

        if (!contact.getCustomFields().isEmpty()) {
            mainPanel.add(new JLabel("自定义字段:"));
            for (Map.Entry<String, String> entry : contact.getCustomFields().entrySet()) {
                mainPanel.add(createInfoField(entry.getKey() + ":", entry.getValue()));
            }
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoField(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.add(new JLabel(label), BorderLayout.WEST);

        JTextField textField = new JTextField(value);
        textField.setEditable(false);
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(textField, BorderLayout.CENTER);

        return panel;
    }
}