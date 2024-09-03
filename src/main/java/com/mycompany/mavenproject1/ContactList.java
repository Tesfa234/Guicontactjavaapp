package com.mycompany.mavenproject1;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ContactList extends JFrame implements ActionListener {
    // Input fields
    private JTextField nameField, addressField, emailField, phoneField;
    private JButton addContactButton;

    // Search field
    private JTextField searchField;

    // Panels
    private JPanel inputPanel, searchPanel, contactPanel;
    private JScrollPane contactScrollPane;

    // Contact data
    private List<String> contacts;
    private List<String> filteredContacts;
    private String selectedContact;
    private JDialog updateDialog;

    public ContactList() {
        // Frame settings
        setTitle("Contact Manager");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize contact lists
        contacts = new ArrayList<>();
        filteredContacts = new ArrayList<>();

        // Initialize components
        initializeInputPanel();
        initializeSearchPanel();
        initializeContactPanel();

        // Load contacts from file
        loadContacts();

        // Layout setup
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);

        // Main panel to hold search and contact panels
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(contactScrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void initializeInputPanel() {
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Contact"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);

        nameField = new JTextField(20);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Address:"), gbc);

        addressField = new JTextField(20);
        gbc.gridx = 1;
        inputPanel.add(addressField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Email:"), gbc);

        emailField = new JTextField(20);
        gbc.gridx = 1;
        inputPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Phone:"), gbc);

        phoneField = new JTextField(20);
        gbc.gridx = 1;
        inputPanel.add(phoneField, gbc);

        // Add Contact Button
        addContactButton = new JButton("Add Contact");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(addContactButton, gbc);

        addContactButton.addActionListener(this);
    }

    private void initializeSearchPanel() {
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Contacts"));

        searchPanel.add(new JLabel("Search:"));

        searchField = new JTextField(30);
        searchPanel.add(searchField);

        // Add a DocumentListener to the search field to handle live search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterContacts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterContacts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterContacts();
            }
        });
    }

    private void initializeContactPanel() {
        contactPanel = new JPanel();
        contactPanel.setLayout(new BoxLayout(contactPanel, BoxLayout.Y_AXIS));
        contactPanel.setPreferredSize(new Dimension(650, 400)); // Adjust height as needed

        contactScrollPane = new JScrollPane(contactPanel);
        contactScrollPane.setBorder(BorderFactory.createTitledBorder("Contact List"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addContactButton) {
            addNewContact();
        }
    }

    private void addNewContact() {
        if (validateFields()) {
            String contact = String.join(",",
                    nameField.getText().trim(),
                    addressField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
            );

            contacts.add(contact);
            saveContactsToFile();
            clearInputFields();
            filterContacts();
            JOptionPane.showMessageDialog(this, "Contact added successfully!");
        }
    }

    private boolean validateFields() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Check if any field is empty
        if (name.isEmpty() || address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate email address
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate phone number (only digits allowed)
        if (!phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Phone number must contain only digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if contact already exists
        for (String contact : contacts) {
            String[] details = contact.split(",", -1);
            if (details[0].equals(name) || details[2].equals(email) || details[3].equals(phone)) {
                JOptionPane.showMessageDialog(this, "Contact with the same name, email, or phone already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void clearInputFields() {
        nameField.setText("");
        addressField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }

    private void loadContacts() {
        contacts.clear();
        File file = new File("contacts.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    contacts.add(line);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading contacts from file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        filterContacts(); // Populate contactPanel with contacts on startup
    }

    private void saveContactsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("contacts.txt"))) {
            for (String contact : contacts) {
                writer.write(contact);
                writer.newLine();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving contacts to file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterContacts() {
        String query = searchField.getText().trim().toLowerCase();
        contactPanel.removeAll();

        if (query.isEmpty()) {
            // Show all contacts if no search query is provided
            for (String contact : contacts) {
                contactPanel.add(createContactPanel(contact));
            }
        } else {
            // Filter and show matching contacts
            for (String contact : contacts) {
                if (contact.toLowerCase().contains(query)) {
                    contactPanel.add(createContactPanel(contact));
                }
            }
        }

        if (contactPanel.getComponentCount() == 0) {
            contactPanel.add(new JLabel("No contacts found."));
        }

        contactPanel.revalidate();
        contactPanel.repaint();
    }

    private JPanel createContactPanel(String contact) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Make individual contact panels a bit taller

        String[] details = contact.split(",", -1);

        // Contact Information
        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.add(new JLabel("Name: " + details[0]));
        infoPanel.add(new JLabel("Address: " + details[1]));
        infoPanel.add(new JLabel("Email: " + details[2]));
        infoPanel.add(new JLabel("Phone: " + details[3]));

        panel.add(infoPanel, BorderLayout.CENTER);

        // Edit Button
        JPanel buttonPanel = new JPanel();
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> showUpdateDialog(contact));
        buttonPanel.add(editButton);

        // Delete Button
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this contact?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                contacts.remove(contact);
                saveContactsToFile();
                filterContacts();
                JOptionPane.showMessageDialog(this, "Contact deleted successfully!");
            }
        });
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void showUpdateDialog(String contact) {
        // Split contact details
        String[] details = contact.split(",", -1);

        // Create a dialog for editing
        updateDialog = new JDialog(this, "Update Contact", true);
        updateDialog.setSize(400, 300);
        updateDialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialogPanel.add(new JLabel("Name:"), gbc);

        JTextField nameField = new JTextField(20);
        nameField.setText(details[0]);
        gbc.gridx = 1;
        dialogPanel.add(nameField, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogPanel.add(new JLabel("Address:"), gbc);

        JTextField addressField = new JTextField(20);
        addressField.setText(details[1]);
        gbc.gridx = 1;
        dialogPanel.add(addressField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialogPanel.add(new JLabel("Email:"), gbc);

        JTextField emailField = new JTextField(20);
        emailField.setText(details[2]);
        gbc.gridx = 1;
        dialogPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialogPanel.add(new JLabel("Phone:"), gbc);

        JTextField phoneField = new JTextField(20);
        phoneField.setText(details[3]);
        gbc.gridx = 1;
        dialogPanel.add(phoneField, gbc);

        // Update Button
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateContact(contact, nameField.getText().trim(), addressField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim()));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialogPanel.add(updateButton, gbc);

        updateDialog.add(dialogPanel);
        updateDialog.setVisible(true);
    }

    private void updateContact(String oldContact, String name, String address, String email, String phone) {
        if (validateFieldsForUpdate(name, address, email, phone)) {
            contacts.remove(oldContact);
            String updatedContact = String.join(",", name, address, email, phone);
            contacts.add(updatedContact);
            saveContactsToFile();
            filterContacts();
            updateDialog.dispose();
            JOptionPane.showMessageDialog(this, "Contact updated successfully!");
        }
    }

    private boolean validateFieldsForUpdate(String name, String address, String email, String phone) {
        // Same validation as before, but exclude the check for existing contacts
        if (name.isEmpty() || address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Phone number must contain only digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ContactList frame = new ContactList();
            frame.setVisible(true);
        });
    }
}
