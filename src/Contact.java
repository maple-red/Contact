import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Contact {
    private String name;
    private String group;
    private List<Phone> phones = new ArrayList<>();
    private List<Email> emails = new ArrayList<>();
    private Map<String, String> customFields = new HashMap<>();
    public Contact() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public List<Phone> getPhones() { return phones; }
    public void setPhones(List<Phone> phones) { this.phones = phones; }
    public void addPhone(Phone phone) { phones.add(phone); }

    public List<Email> getEmails() { return emails; }
    public void setEmails(List<Email> emails) { this.emails = emails; }
    public void addEmail(Email email) { emails.add(email); }

    public Map<String, String> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, String> customFields) { this.customFields = customFields; }
    public void addCustomField(String key, String value) { customFields.put(key, value); }

    @Override
    public String toString() {
        return name;
    }
    public static class Phone {
        private String type;
        private String number;

        public Phone(String type, String number) {
            this.type = type;
            this.number = number;
        }

        public String getType() { return type; }
        public String getNumber() { return number; }

        @Override
        public String toString() {
            return type + "：" + number;
        }
    }

    public static class Email {
        private String type;
        private String address;

        public Email(String type, String address) {
            this.type = type;
            this.address = address;
        }

        public String getType() { return type; }
        public String getAddress() { return address; }

        @Override
        public String toString() { return type + "：" + address;
        }
    }
}