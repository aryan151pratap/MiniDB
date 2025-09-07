package MiniDB.console;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ConsoleGui extends JFrame {
    private JTextPane consoleArea;
    private StyledDocument doc;
    private Style promptStyle, inputStyle, outputStyle, errorStyle, headStyle, showStyle, purpleStyle, showDataStyle;
    private String prompt = "MiniDB> ";
    private List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    private int inputStartPosition = 0;
    
    // Auto-completion variables
    private JList<String> suggestionList;
    private JWindow suggestionWindow;
    private DefaultListModel<String> suggestionModel;
    private List<String> availableCommands;
    private int currentSuggestionIndex = -1;

    public ConsoleGui() {
        ImageIcon img = new ImageIcon("MiniDB\\console\\MiniDB_image.png");
        
        setTitle("MiniDB Console");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setIconImage(img.getImage());

        // Initialize available commands
        initializeAvailableCommands();

        // Console area
        consoleArea = new JTextPane();
        consoleArea.setBackground(new Color(10, 10, 10));
        consoleArea.setCaretColor(Color.WHITE);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        consoleArea.setEditable(true);
        consoleArea.setMargin(new Insets(5, 5, 5, 5));

        doc = consoleArea.getStyledDocument();
        initStyles();

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Add status bar
        add(createStatusBar(), BorderLayout.SOUTH);

        // Initialize suggestion window
        initSuggestionWindow();

        // Key listener
        consoleArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    int caretPos = consoleArea.getCaretPosition();

                    // Prevent editing before inputStartPosition
                    if (caretPos < inputStartPosition &&
                        e.getKeyCode() != KeyEvent.VK_LEFT &&
                        e.getKeyCode() != KeyEvent.VK_RIGHT) {
                        e.consume();
                        consoleArea.setCaretPosition(doc.getLength());
                        return;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE &&
                        caretPos <= inputStartPosition) {
                        e.consume();
                        return;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        e.consume();
                        if (suggestionWindow.isVisible()) {
                            acceptSuggestion();  // accept suggestion on Enter
                        } else {
                            processCommand();    // normal Enter
                        }
                    }else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        e.consume();
                        if (suggestionWindow.isVisible()) {
                            navigateSuggestions(-1);
                        } else {
                            showPreviousCommand();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        e.consume();
                        if (suggestionWindow.isVisible()) {
                            navigateSuggestions(1);
                        } else {
                            showNextCommand();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
                        e.consume();
                        if (suggestionWindow.isVisible()) {
                            acceptSuggestion();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        e.consume();
                        hideSuggestions();
                    } else {
                        // Other keys - show suggestions after a short delay
                        SwingUtilities.invokeLater(() -> {
                            updateSuggestions();
                        });
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Welcome message
        displayWelcomeMessage();
        inputStartPosition = doc.getLength();
    }

    private void initializeAvailableCommands() {
        availableCommands = new ArrayList<>();
        availableCommands.add("help");
        availableCommands.add("version");
        availableCommands.add("exit");
        availableCommands.add("show databases");
        availableCommands.add("use");
        availableCommands.add("show");
        availableCommands.add("show collections");
        availableCommands.add("db.create");
        availableCommands.add("db.createcollection");
        availableCommands.add("db.insert");
        availableCommands.add("db.findOne");
        availableCommands.add("db.findAll");
        availableCommands.add("db.deleteDoc");
        availableCommands.add("db.getTime");
        availableCommands.add("db.updateDoc");
        availableCommands.add("drop");
        availableCommands.add("drop collection");
        availableCommands.add("clear");
    }

    private void initSuggestionWindow() {
        suggestionWindow = new JWindow(this);
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);

        suggestionList.setBackground(new Color(40, 40, 40));
        suggestionList.setForeground(Color.WHITE);
        suggestionList.setSelectionBackground(new Color(0, 120, 215));
        suggestionList.setSelectionForeground(Color.WHITE);
        suggestionList.setFont(new Font("Consolas", Font.PLAIN, 12));
        suggestionList.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        // prevent stealing focus
        suggestionWindow.setFocusableWindowState(false);
        suggestionList.setFocusable(false);

        suggestionWindow.getContentPane().add(new JScrollPane(suggestionList));
        suggestionWindow.setSize(300, 150);

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    acceptSuggestion();
                }
            }
        });
    }


    private void updateSuggestions() {
        try {
            String currentText = doc.getText(inputStartPosition, doc.getLength() - inputStartPosition);
            if (currentText.isEmpty()) {
                hideSuggestions();
                return;
            }

            suggestionModel.clear();
            for (String cmd : availableCommands) {
                if (cmd.toLowerCase().startsWith(currentText.toLowerCase())) {
                    suggestionModel.addElement(cmd);
                }
            }

            if (suggestionModel.isEmpty()) {
                hideSuggestions();
                return;
            }

            Rectangle caretRect = consoleArea.modelToView(consoleArea.getCaretPosition());
            Point location = new Point(caretRect.x, caretRect.y + caretRect.height);
            SwingUtilities.convertPointToScreen(location, consoleArea);

            suggestionWindow.setLocation(location);
            suggestionWindow.setVisible(true);
            suggestionList.setSelectedIndex(0);
            currentSuggestionIndex = 0;

            consoleArea.requestFocusInWindow(); // ⬅️ important
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }


    private void hideSuggestions() {
        suggestionWindow.setVisible(false);
        currentSuggestionIndex = -1;
    }

    private void navigateSuggestions(int direction) {
        if (suggestionModel.isEmpty()) return;
        
        int newIndex = suggestionList.getSelectedIndex() + direction;
        if (newIndex >= 0 && newIndex < suggestionModel.size()) {
            suggestionList.setSelectedIndex(newIndex);
            currentSuggestionIndex = newIndex;
        }
    }

    private void acceptSuggestion() {
        if (currentSuggestionIndex >= 0 && currentSuggestionIndex < suggestionModel.size()) {
            String selectedSuggestion = suggestionModel.getElementAt(currentSuggestionIndex);
            try {
                doc.remove(inputStartPosition, doc.getLength() - inputStartPosition);
                doc.insertString(inputStartPosition, selectedSuggestion, inputStyle);
                consoleArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        hideSuggestions();
        consoleArea.requestFocusInWindow();
    }


    private void initStyles() {
        promptStyle = doc.addStyle("prompt", null);
        StyleConstants.setForeground(promptStyle, new Color(0, 200, 80));
        StyleConstants.setBold(promptStyle, true);

        inputStyle = doc.addStyle("input", null);
        StyleConstants.setForeground(inputStyle, new Color(0xE14DE1FF, true));

        outputStyle = doc.addStyle("output", null);
        StyleConstants.setForeground(outputStyle, new Color(200, 200, 0));

        headStyle = doc.addStyle("heading", null);
        StyleConstants.setForeground(headStyle, Color.WHITE);

        showStyle = doc.addStyle("showStyle", null);
        StyleConstants.setForeground(showStyle, new Color(255, 0, 192));

        showDataStyle = doc.addStyle("showDataStyle", null);
        StyleConstants.setForeground(showDataStyle, new Color(65, 130, 255));

        purpleStyle = doc.addStyle("purpleStyle", null);
        StyleConstants.setForeground(purpleStyle, new Color(228, 22, 100));

        errorStyle = doc.addStyle("error", null);
        StyleConstants.setForeground(errorStyle, new Color(255, 100, 100));
        StyleConstants.setBold(errorStyle, true);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(new EmptyBorder(2, 5, 2, 5));
        statusBar.setBackground(new Color(60, 60, 60));
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusBar.add(statusLabel);
        return statusBar;
    }

    private void displayWelcomeMessage() {
		try {
			doc.remove(0, doc.getLength());
            doc.insertString(doc.getLength(), "Welcome to MiniDB Console\n", headStyle);
            doc.insertString(doc.getLength(), "Version 2.0 - Type 'help' for available commands\n\n", headStyle);
            addPrompt();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void addPrompt() throws BadLocationException {
        doc.insertString(doc.getLength(), prompt, promptStyle);
        inputStartPosition = doc.getLength();
        consoleArea.setCaretPosition(inputStartPosition);
    }

    private void processCommand() throws BadLocationException {
        String command = doc.getText(inputStartPosition, doc.getLength() - inputStartPosition).trim();

        if (!command.isEmpty()) {
            commandHistory.add(command);
            historyIndex = commandHistory.size();
            String response = executeCommand(command);
            if (response.startsWith("Error:")) {
                doc.insertString(doc.getLength(), "\n" + response + "\n", errorStyle);
            } else if (response.equals("help")) {
                doc.insertString(doc.getLength(), "\n" + response + "\n", headStyle);
            } else if (response.startsWith("Databases") || response.startsWith("Collections") || response.startsWith("Documents")) {
                doc.insertString(doc.getLength(), "\n" + response + "\n", showStyle);
            } else if (response.startsWith("Database (")) {
                doc.insertString(doc.getLength(), "\n" + response + "\n", headStyle);
            } else if (response.startsWith("[") || response.startsWith("{")) {
                doc.insertString(doc.getLength(), "\n" + response + "\n", purpleStyle);
            } else {
                doc.insertString(doc.getLength(), "\n" + response + "\n", outputStyle);
            }
        } else {
		    doc.insertString(doc.getLength(), "\n", outputStyle);
        }
        if (command.equalsIgnoreCase("clear")) {
			displayWelcomeMessage();
		} else {
			addPrompt();
		}
    }

    private void showPreviousCommand() throws BadLocationException {
        if (commandHistory.isEmpty()) return;
        if (historyIndex > 0) {
            historyIndex--;
            replaceCurrentInput(commandHistory.get(historyIndex));
        }
    }

    private void showNextCommand() throws BadLocationException {
        if (commandHistory.isEmpty()) return;
        if (historyIndex < commandHistory.size() - 1) {
            historyIndex++;
            replaceCurrentInput(commandHistory.get(historyIndex));
        } else {
            historyIndex = commandHistory.size();
            replaceCurrentInput("");
        }
    }

    private void replaceCurrentInput(String newInput) throws BadLocationException {
        doc.remove(inputStartPosition, doc.getLength() - inputStartPosition);
        doc.insertString(inputStartPosition, newInput, inputStyle);
        consoleArea.setCaretPosition(doc.getLength());
    }

    private String executeCommand(String command) {
        switch (command.toLowerCase()) {
            case "help":
                return "Commands:\n\n -help \n -version \n -exit \n -show databases \n -use <Database_name> \n -show \n -show collections \n -db.createcollection.(<collection_name>) \n -db.<collection_name>.insert({Key:value}) \n -db.<collection_name>.findOne() \n -db.<collection_name>.findAll() \n -db.<collection_name>.findOne().valueof({<KeyName>, <KeyName-ObjectKeyName>}) \n -db.<collection_name>.findAll().valueof({<KeyName>, <KeyName-ObjectKeyName>}) \n -db.<collection_name>.deleteDoc({Key:value}) \n -db.<collection_name>.getTime({Key:value}) \n -db.<collection_name>.updateDoc({Key:value}).set({Key:value}) \n -drop <Database_name> \n -drop collection <collection_name> \n";
            case "version":
                return "MiniDB Version 2.0";
            case "exit":
                System.exit(0);
                return "";
			case "clear":
				try {
					doc.remove(0, doc.getLength());
					addPrompt();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
            return "";
            default:
				return MiniDB.App.execute_Command(command);        
		}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConsoleGui gui = new ConsoleGui();
            gui.setLocationRelativeTo(null);
            gui.setVisible(true);
        });
    }
}
