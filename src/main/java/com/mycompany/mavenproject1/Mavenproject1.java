package com.mycompany.mavenproject1;

/**
 *
 * @author Tesfa
 */
public class Mavenproject1 {

    public static void main(String[] args) {
        // Run the ContactList GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ContactList().setVisible(true);
        });
    }
}
