package main.cromero.gui;


import main.cromero.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;

public class MainForm extends JDialog {

    //region Graphic code components and start app
    private JPanel contentPane;
    private JButton btnRecord;
    private JButton btnExit;
    private JTextArea textHistory;
    private JTextArea textCurrent;
    private JButton btnStop;
    private JButton btnDelete;
    private JButton btnCopy;

    SpeechAnalysisServices serviceAnalysis;

    public MainForm() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnRecord);

        btnRecord.setEnabled(true);
        btnStop.setEnabled(false);

        btnRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRecord();
            }
        });

        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStop();
            }
        });

        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onExit();
            }
        });

        btnCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearTextHistory();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onExit();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        addjustInitialForm();

        startPropertiesListener();
        setupSystemTray();
    }

    private void addjustInitialForm() {

        Container parent = textHistory.getParent();
        while (parent != null && !(parent instanceof JSplitPane)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            JSplitPane splitPane = (JSplitPane) parent;
            splitPane.setDividerLocation(100); // Set the divider location in pixels
        }
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/speech_32.png"));

        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> onExit());
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "Speech Analysis", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> setVisible(true));

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    public static void main(String[] args) {
        MainForm dialog = new MainForm();
        dialog.setSize(700, 300);
        dialog.setVisible(true);
        dialog.setTitle("Central Form");
        dialog.pack();
        System.exit(0);
    }
    //endregion

    private void startPropertiesListener() {
        serviceAnalysis = new SpeechAnalysisServices();

        serviceAnalysis.addPropertyChangeListener(evt -> {

            if ("oldText".equals(evt.getPropertyName())) {
                textHistory.append((String) evt.getNewValue() + "\n");
                textHistory.setCaretPosition(textHistory.getDocument().getLength());
                System.out.println("CurrentText changed: " + evt.getNewValue());

            } else if ("currentText".equals(evt.getPropertyName())) {
                textCurrent.setText((String) evt.getNewValue());
                System.out.println("OldText changed: " + evt.getNewValue());

            }
        });
    }

    //region Actions on buttons

    private void onStop() {
        serviceAnalysis.stop();
        btnRecord.setEnabled(true);
        btnStop.setEnabled(false);
    }

    private void onRecord() {
//        serviceAnalysis.execute();
        serviceAnalysis.record();
        btnRecord.setEnabled(false);
        btnStop.setEnabled(true);
    }

    private void onExit() {
        dispose();
    }

    // Method to copy textHistory content to clipboard
    private void copyToClipboard() {
        String textToCopy = textHistory.getText();
        if (!textToCopy.isEmpty()) {
            StringSelection stringSelection = new StringSelection(textToCopy);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    // Method to clear the content of textHistory
    private void clearTextHistory() {
        textHistory.setText("");
    }

    //endregion

}
