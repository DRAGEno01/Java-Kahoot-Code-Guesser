import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Robot;
import java.awt.AWTException;
import java.util.Random;
import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.net.URI;
import java.awt.Desktop;
import java.util.List;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.SwingConstants;

public class KahootCodeGuesser extends JFrame {
    private static final String AUTHOR_SIGNATURE = "DRAGEno01";
    private static final Color GRADIENT_START = new Color(46, 14, 123);  // Darker purple
    private static final Color GRADIENT_END = new Color(115, 40, 220);   // Lighter purple
    private static final Color RED_BUTTON = new Color(226, 27, 60);      // Kahoot Red
    private static final Color RED_DISABLED = new Color(150, 50, 70);    // Disabled red
    private static final Color BLUE_BUTTON = new Color(19, 104, 206);    // Kahoot Blue
    private static final Color BLUE_DISABLED = new Color(50, 80, 150);   // Disabled blue
    private static final Color YELLOW_BUTTON = new Color(216, 158, 0);   // Kahoot Yellow
    private static final Color GREEN_BUTTON = new Color(38, 137, 12);    // Kahoot Green
    private static final Color COUNTER_BG = new Color(46, 23, 155);      // Counter background
    
    private JButton startButton;
    private JButton stopButton;
    private Timer numberTimer;
    private Timer backspaceTimer;
    private Timer initialDelayTimer;
    private Robot robot;
    private Random random;
    private boolean isRunning;
    private int attemptCount = 0;
    private List<String> attemptedCodes = new ArrayList<>();
    private JFrame historyWindow;

    public KahootCodeGuesser() {
        if (!validateIntegrity()) {
            System.err.println("Invalid modification detected!");
            System.exit(1);
        }
        setTitle("Kahoot Code Guesser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        
        random = new Random();
        
        // Create main panel with gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, 0, h, GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Update title area to match Kahoot style
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        // Add header
        JLabel headerLabel = new JLabel("Kahoot Code Guesser", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        // Create attempts counter panel
        JPanel attemptsPanel = createAttemptsPanel();
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        
        topPanel.add(attemptsPanel, BorderLayout.WEST);
        topPanel.add(headerPanel, BorderLayout.CENTER);
        
        // Update main button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        startButton = createStyledButton("Start Guessing", RED_BUTTON, RED_DISABLED, "⨂");
        stopButton = createStyledButton("Stop Guessing", BLUE_BUTTON, BLUE_DISABLED, "⨂");
        JButton kahootButton = createStyledButton("Kahoot.it", YELLOW_BUTTON, YELLOW_BUTTON, "⨂");
        JButton githubButton = createStyledButton("GitHub Repo", GREEN_BUTTON, GREEN_BUTTON, "⨂");
        

        // Add button actions
        startButton.addActionListener(e -> startPractice());
        stopButton.addActionListener(e -> stopPractice());
        kahootButton.addActionListener(e -> openWebpage("https://kahoot.it/"));
        githubButton.addActionListener(e -> openWebpage("https://github.com/DRAGEno01/Java-Kahoot-Code-Guesser/"));
        
        stopButton.setEnabled(false);
        
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(kahootButton);
        buttonPanel.add(githubButton);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Add credit label at bottom
        JLabel creditLabel = new JLabel("Created by DRAGEno01", SwingConstants.CENTER);
        creditLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        creditLabel.setForeground(new Color(100, 100, 100));
        creditLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        mainPanel.add(creditLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        initialDelayTimer = new Timer(500, e -> {
            initialDelayTimer.stop();
            numberTimer.start();
        });
        
        numberTimer = new Timer(1000, e -> typeNumber());
        backspaceTimer = new Timer(500, e -> performBackspace());
        
        // Set window properties
        setSize(650, 450);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private JButton createStyledButton(String text, Color enabledColor, Color disabledColor, String symbol) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Draw button background with shadow
                if (isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRect(2, 2, w-2, h-2);
                    g2.setColor(getModel().isPressed() ? enabledColor.darker() : 
                               getModel().isRollover() ? enabledColor.brighter() : 
                               enabledColor);
                } else {
                    g2.setColor(disabledColor);
                }
                g2.fillRect(0, 0, w-2, h-2);
                
                // Draw symbol with slight shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                FontMetrics symbolFm = g2.getFontMetrics();
                g2.drawString(symbol, h/4 + 1, (h + symbolFm.getAscent() - symbolFm.getDescent()) / 2 + 1);
                
                g2.setColor(Color.WHITE);
                g2.drawString(symbol, h/4, (h + symbolFm.getAscent() - symbolFm.getDescent()) / 2);
                
                // Draw text with shadow
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = h + (w - h - fm.stringWidth(getText()))/2;
                int textY = ((h - fm.getHeight()) / 2) + fm.getAscent();
                
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawString(getText(), textX + 1, textY + 1);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), textX, textY);
                
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                int width = fm.stringWidth(getText()) + getHeight() + 40; // Add space for shape
                return new Dimension(width, 60); // Fixed height for Kahoot-style buttons
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(enabledColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(enabledColor);
            }
        });
        
        return button;
    }
    private static final String VALIDATION_KEY = "k7h8j4n1m9p3";
    private JPanel createAttemptsPanel() {
        JPanel attemptsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval(3, 3, getWidth()-3, getWidth()-3);
                
                // Draw counter circle
                g2d.setColor(COUNTER_BG);
                g2d.fillOval(0, 0, getWidth()-3, getWidth()-3);
                
                // Draw attempts number
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 32));
                String attempts = String.valueOf(attemptCount);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(attempts, 
                    (getWidth() - fm.stringWidth(attempts)) / 2 - 1,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(90, 90);
            }
        };
        attemptsPanel.setOpaque(false);
        attemptsPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 0, 0));
        attemptsPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add click listener
        attemptsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showHistoryWindow();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                attemptsPanel.setToolTipText("Click to view attempted codes");
            }
        });
        
        return attemptsPanel;
    }
    
    private void typeNumber() {
        if (!validateIntegrity()) {
            stopPractice();
            return;
        }
        if (!isRunning) return;
        numberTimer.stop();
        
        attemptCount++;
        attemptedCodes.add(String.valueOf(random.nextInt(900000) + 100000));
        repaint(); // Update the attempts counter
        
        // Update to generate 6-digit codes (Kahoot format)
        int randomNumber = random.nextInt(900000) + 100000;
        String numberStr = String.valueOf(randomNumber);
        
        // Type each digit faster
        for (char digit : numberStr.toCharArray()) {
            int keyCode = Character.getNumericValue(digit) + KeyEvent.VK_0;
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
            robot.delay(25); // Reduced from 50ms to 25ms between keystrokes
        }
        
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        
        backspaceTimer.restart();
    }

    private void startPractice() {
        if (!validateIntegrity() || !AUTHOR_SIGNATURE.equals("DRAGEno01")) {
            return;
        }
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        initialDelayTimer.start();
    }
    
    private void stopPractice() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        initialDelayTimer.stop();
        numberTimer.stop();
        backspaceTimer.stop();
    }
    
    private void performBackspace() {
        if (!isRunning) return;
        backspaceTimer.stop();
        
        // Press Ctrl+A to select all
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        
        // Single backspace
        robot.keyPress(KeyEvent.VK_BACK_SPACE);
        robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        
        numberTimer.restart();
    }
    
    private void openWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Could not open the link. Please visit: " + url,
                "Error Opening Link",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showHistoryWindow() {
        if (historyWindow != null && historyWindow.isVisible()) {
            historyWindow.requestFocus();
            return;
        }
        
        historyWindow = new JFrame("Attempted Codes History");
        historyWindow.setLayout(new BorderLayout());
        
        // Create the list model and JList
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (int i = attemptedCodes.size() - 1; i >= 0; i--) {
            listModel.addElement("Code #" + (i + 1) + ": " + attemptedCodes.get(i));
        }
        
        JList<String> codeList = new JList<>(listModel);
        codeList.setFont(new Font("Arial", Font.BOLD, 16));
        codeList.setBackground(new Color(46, 14, 123));
        codeList.setForeground(Color.WHITE);
        codeList.setSelectionBackground(new Color(115, 40, 220));
        codeList.setSelectionForeground(Color.WHITE);
        
        // Add padding to the list items
        codeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return label;
            }
        });
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(codeList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(115, 40, 220);
                this.trackColor = new Color(46, 14, 123);
            }
        });
        
        // Add a title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(46, 14, 123));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel titleLabel = new JLabel("Attempted Codes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        historyWindow.add(titlePanel, BorderLayout.NORTH);
        historyWindow.add(scrollPane, BorderLayout.CENTER);
        
        // Set window properties
        historyWindow.setSize(300, 400);
        historyWindow.setLocationRelativeTo(null);
        historyWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Make the window non-resizable and always on top
        historyWindow.setResizable(false);
        historyWindow.setAlwaysOnTop(true);
        
        historyWindow.setVisible(true);
    }
    
    private boolean validateIntegrity() {
        String combined = AUTHOR_SIGNATURE + VALIDATION_KEY;
        return combined.hashCode() == 1245649099
            && AUTHOR_SIGNATURE.equals("DRAGEno01");
    }
    
    public static void main(String[] args) {
        String validationCheck = AUTHOR_SIGNATURE + VALIDATION_KEY;
        if (validationCheck.hashCode() != 1245649099) {
            System.err.println("Validation failed!");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            KahootCodeGuesser app = new KahootCodeGuesser();
            app.setVisible(true);
        });
    }
}
