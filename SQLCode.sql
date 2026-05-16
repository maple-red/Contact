CREATE DATABASE contact_db;
USE contact_db;
CREATE TABLE contacts (
  name VARCHAR(100) NOT NULL PRIMARY KEY,
  group_name VARCHAR(100)
);

CREATE TABLE phones (
  contact_name VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20) NOT NULL,
  phone_type VARCHAR(20) DEFAULT '手机',
  FOREIGN KEY (contact_name) REFERENCES contacts(name) ON DELETE CASCADE
);

CREATE TABLE emails (
  contact_name VARCHAR(100) NOT NULL,
  email_address VARCHAR(100) NOT NULL,
  email_type VARCHAR(20) DEFAULT '邮箱',
  FOREIGN KEY (contact_name) REFERENCES contacts(name) ON DELETE CASCADE
);

CREATE TABLE custom_fields (
  contact_name VARCHAR(100) NOT NULL,
  field_key VARCHAR(50) NOT NULL,
  field_value VARCHAR(255) NOT NULL,
  FOREIGN KEY (contact_name) REFERENCES contacts(name) ON DELETE CASCADE
);
ALTER TABLE phones DROP FOREIGN KEY phones_ibfk_1;
ALTER TABLE emails DROP FOREIGN KEY emails_ibfk_1;
ALTER TABLE custom_fields DROP FOREIGN KEY custom_fields_ibfk_1;


ALTER TABLE phones 
ADD CONSTRAINT fk_phones_contacts 
FOREIGN KEY (contact_name) 
REFERENCES contacts(name) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

ALTER TABLE emails 
ADD CONSTRAINT fk_emails_contacts 
FOREIGN KEY (contact_name) 
REFERENCES contacts(name) 
ON DELETE CASCADE 
ON UPDATE CASCADE;

ALTER TABLE custom_fields 
ADD CONSTRAINT fk_custom_fields_contacts 
FOREIGN KEY (contact_name) 
REFERENCES contacts(name) 
ON DELETE CASCADE 
ON UPDATE CASCADE;