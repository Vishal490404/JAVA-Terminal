import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class TerminalApp extends JFrame implements ActionListener, MouseWheelListener {
    private JTextField commandField;
    private JTextPane outputPane;
    private StyledDocument document;
    private Style style;
    private float fontSize = 14.0f;
    private List<String> commandHistory;
    private int historyIndex;
    private String welcomeMessage = "Welcome to the Terminal!\n\nTo know the commands type HELP!\n\n";

    public TerminalApp() {
        setTitle("Terminal");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        commandHistory = new ArrayList<>();
        historyIndex = -1; 

        commandField = new JTextField("Type command here...");
        commandField.setForeground(Color.BLACK);
        commandField.addActionListener(this);
        commandField.setPreferredSize(new Dimension(commandField.getPreferredSize().width, 35));

        Font fieldFont = commandField.getFont();
        commandField.setFont(new Font(fieldFont.getName(), Font.BOLD, fieldFont.getSize()));

        commandField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (commandField.getText().equals("Type command here...")) {
                    commandField.setText("");
                    commandField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (commandField.getText().isEmpty()) {
                    commandField.setText("Type command here...");
                    commandField.setForeground(Color.BLACK);
                }
            }
        });

        // Add key listener to command field
        commandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (!commandHistory.isEmpty()) {
                        if (historyIndex == -1) { // If index is at -1, start from the end
                            historyIndex = commandHistory.size() - 1;
                        } else if (historyIndex > 0) { // If not at the beginning, decrement
                            historyIndex--;
                        }
                        commandField.setText(commandHistory.get(historyIndex));
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!commandHistory.isEmpty()) {
                        if (historyIndex < commandHistory.size() - 1) { // If not at the end, increment
                            historyIndex++;
                            commandField.setText(commandHistory.get(historyIndex));
                        } else if (historyIndex == commandHistory.size() - 1) { // If at the end, clear the field
                            historyIndex = -1;
                            commandField.setText("");
                        }
                    }
                }
            }
        });

        add(commandField, BorderLayout.SOUTH);

        // Output pane
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(Color.BLACK);
        outputPane.setForeground(Color.WHITE);
        document = outputPane.getStyledDocument();
        style = outputPane.addStyle("Style", null);
        setFontSize(fontSize);
        JScrollPane scrollPane = new JScrollPane(outputPane);
        scrollPane.addMouseWheelListener(this);
        add(scrollPane, BorderLayout.CENTER);

        appendToPane(outputPane, "Welcome to the Terminal!\n\nTo know the commands type HELP!\n\n", Color.orange, true);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = commandField.getText();
        commandHistory.add(command);
        historyIndex = -1; // Reset index after a new command is entered
        appendToPane(outputPane, "> " + command + "\n", Color.WHITE, true);
        String output = processCommand(command);
        appendToPane(outputPane, output + "\n", Color.GREEN, false);
        commandField.setText("");

        if (command.equalsIgnoreCase("exit") || (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK
                && e.getActionCommand().equals("D")) {
            closeTerminal();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
            int rotation = e.getWheelRotation();
            fontSize -= rotation;
            setFontSize(fontSize);
        }
    }

    private String processCommand(String command) {
        String[] parts = command.trim().split("\\s+", 2);
        String keyword = parts[0];
        keyword = keyword.toLowerCase();

        switch (keyword) {
            case "date":
                LocalDateTime currentTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = currentTime.format(formatter);
                return "Current date and time: " + formattedDateTime;
            case "open":
                if (parts.length < 2) {
                    return "Please specify a website to open.";
                }
                String website = parts[1];
                try {
                    if (!website.startsWith("http://") && !website.startsWith("https://")) {
                        website = "https://" + website;
                    }
                    Desktop.getDesktop().browse(new URI(website));
                    return "Opening " + website + "...";
                } catch (URISyntaxException | UnsupportedOperationException | IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to open website.";
                }
            case "calc":
                try {
                    List<String> commandList = new ArrayList<>();
                    commandList.add("calc");
                    ProcessBuilder builder = new ProcessBuilder(commandList);
                    builder.start();
                    return "Opening calculator...";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to open calculator.";
                }
            case "notes":
                try {
                    List<String> commandList = new ArrayList<>();
                    commandList.add("notepad");
                    ProcessBuilder builder = new ProcessBuilder(commandList);
                    builder.start();
                    return "Opening notepad...";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to open notepad.";
                }
            case "files":
                try {
                    List<String> commandList = new ArrayList<>();
                    commandList.add("explorer");
                    ProcessBuilder builder = new ProcessBuilder(commandList);
                    builder.start();
                    return "Opening explorer...";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to open explorer.";
                }
            case "cls":
                clearCommands();
                return "Commands cleared.";

            case "moodle":
                try {
                    Desktop.getDesktop().browse(new URI("http://115.247.30.149/"));
                    return "Opening Moodle website...";
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    return "Error: Unable to open Moodle website.";
                }
            case "erp":
                try {
                    Desktop.getDesktop().browse(new URI("http://wic.walchandsangli.ac.in/"));
                    return "Opening WCE ERP website...";
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    return "Error: Unable to open ERP website.";
                }
            case "slogin":
                try {
                    Desktop.getDesktop().browse(new URI("https://uni.wcoeapps.in/landing"));
                    return "Opening WCE Student Login website...";
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                    return "Error: Unable to open Student Login website.";
                }
            case "word":
                try {
                    Desktop.getDesktop()
                            .open(new File("C:\\Program Files\\Microsoft Office\\root\\Office16\\WINWORD.EXE"));
                    return "Opening Microsoft Word...";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to open Microsoft Word.";
                }
            case "ppt":
                try {
                    Desktop.getDesktop()
                            .open(new File("C:\\Program Files\\Microsoft Office\\root\\Office16\\POWERPNT.EXE"));
                    return "Opening Microsoft PowerPoint...";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to open Microsoft PowerPoint.";
                }
            case "shutdown":
                try {
                    ProcessBuilder shutdownProcess = new ProcessBuilder("shutdown", "/s", "/t", "0");
                    shutdownProcess.start();
                    return "Shutting down the PC...";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to shutdown the PC.";
                }
            
            case "restart":
                try {
                    ProcessBuilder restartProcess = new ProcessBuilder("shutdown", "/r", "/t", "0");
                    restartProcess.start();
                    return "Restarting the PC...";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Unable to restart the PC.";
                }
            
            case "help":
                return "Available commands(Commands are case insensitive):\n" +
                        "date - Display current date and time.\n" +
                        "open [website] - Open a website in the default web browser.\n" +
                        "calc - Open the calculator.\n" +
                        "notes - Open Notepad.\n" +
                        "files - Open File Explorer.\n" +
                        "cls - Clear the commands.\n" +
                        "moodle - Open the WCE Moodle website.\n" +
                        "erp - Open the WCE ERP website.\n" +
                        "slogin - Open the WCE Student Login website.\n" +
                        "word - Open Microsoft Word.\n" +
                        "ppt - Open Microsoft PowerPoint.\n" +
                        "shutdown - Shutdown the pc\n" +
                        "restart - Restart the pc\n" +
                        "exit - Close the terminal.\n";

            default:
                return "Unknown command: " + command;
        }
    }

    private void appendToPane(JTextPane tp, String msg, Color c, boolean bold) {
        StyleConstants.setForeground(style, c);
        StyleConstants.setBold(style, bold);
        try {
            document.insertString(document.getLength(), msg, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void setFontSize(float size) {
        outputPane.setFont(outputPane.getFont().deriveFont(size));
    }

    private void closeTerminal() {
        dispose();
    }

    private void clearCommands() {
        try {
            int commandStart = welcomeMessage.length();
            int commandEnd = document.getLength();
            document.remove(commandStart, commandEnd - commandStart);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TerminalApp::new);
    }
}
