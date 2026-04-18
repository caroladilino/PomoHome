import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.*;
import javax.swing.*;

public class PomodoroUI {

    private int timeLeft = 25 * 60;
    private int defaultTime = 25 * 60;

    private boolean isPaused = true;
    private boolean isRunning = false;

    private ScheduledExecutorService scheduler;

    private JLabel timerLabel;
    private JPanel arrowPanel;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;

    public PomodoroUI() {

        JFrame frame = new JFrame("Gerenciador de Timer");
        frame.setSize(520, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(245, 245, 250));

        // 🔝 TÍTULO
        JLabel title = new JLabel("Gerenciador de Timer", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 📦 CONTAINER BONITO
        JPanel container = new RoundedPanel(30);
        container.setLayout(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // ⏱ TIMER LABEL
        timerLabel = new JLabel(formatTime(timeLeft), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        timerLabel.setForeground(new Color(60, 60, 60));

        // 🔼🔽 SETAS DE CONTROLE
        arrowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        arrowPanel.setOpaque(false);

        JButton upArrow = createArrowButton("▲");
        JButton downArrow = createArrowButton("▼");

        // Ações das setas (aumenta ou diminui 5 minutos)
        upArrow.addActionListener(e -> adjustTime(5));
        downArrow.addActionListener(e -> adjustTime(-5));

        arrowPanel.add(downArrow);
        arrowPanel.add(upArrow);

        // PAINEL CENTRAL (Timer + Setas)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(timerLabel, BorderLayout.CENTER);
        centerPanel.add(arrowPanel, BorderLayout.SOUTH);

        // 🔘 BOTÕES DE CONTROLE
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        startButton = new GradientButton("start");
        pauseButton = new GradientButton("pause");
        resetButton = new GradientButton("reset");

        buttonPanel.add(startButton);

        // Ações dos botões
        startButton.addActionListener(e -> startTimer());
        pauseButton.addActionListener(e -> togglePause());
        resetButton.addActionListener(e -> resetTimer());

        // Montando o layout
        container.add(centerPanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(title, BorderLayout.NORTH);
        frame.add(container, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);
    }

    // ⚙️ LÓGICA DE AJUSTE DE TEMPO
    private void adjustTime(int minutesToChange) {
        if (isRunning) return; // Impede alterar enquanto roda

        int currentMinutes = defaultTime / 60;
        int newMinutes = currentMinutes + minutesToChange;

        // Limites: Máximo de 99 min e Mínimo de 1 min
        if (newMinutes > 95) newMinutes = 95;
        if (newMinutes < 5) newMinutes = 5;

        defaultTime = newMinutes * 60;
        timeLeft = defaultTime;
        timerLabel.setText(formatTime(timeLeft));
    }

    // 🎨 CRIAÇÃO DAS SETAS
    private JButton createArrowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        btn.setForeground(new Color(150, 150, 160));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Efeito de hover (muda a cor ao passar o mouse)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(new Color(80, 80, 90));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(new Color(150, 150, 160));
            }
        });
        
        return btn;
    }

    private void startTimer() {
        if (isRunning) return;

        isRunning = true;
        isPaused = false;
        arrowPanel.setVisible(false); // Esconde as setas ao iniciar

        JPanel parent = (JPanel) startButton.getParent();
        parent.removeAll();
        parent.add(pauseButton);
        parent.add(resetButton);
        parent.revalidate();
        parent.repaint();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            if (!isPaused) {
                timeLeft--;

                SwingUtilities.invokeLater(() ->
                        timerLabel.setText(formatTime(timeLeft))
                );

                if (timeLeft <= 0) {
                    Toolkit.getDefaultToolkit().beep();
                    SwingUtilities.invokeLater(this::resetTimer);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "resume" : "pause");
    }

    private void resetTimer() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        isRunning = false;
        isPaused = true;
        arrowPanel.setVisible(true); // Mostra as setas novamente

        timeLeft = defaultTime;
        timerLabel.setText(formatTime(timeLeft));

        JPanel parent = (JPanel) pauseButton.getParent();
        if (parent != null) {
            parent.removeAll();
            parent.add(startButton);
            parent.revalidate();
            parent.repaint();
        }

        pauseButton.setText("pause");
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(PomodoroUI::new);
    }
}

// 🎨 CONTAINER ARREDONDADO
class RoundedPanel extends JPanel {
    private int radius;

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));

        g2.dispose();
        super.paintComponent(g);
    }
}

// 🎨 BOTÃO GRADIENTE
class GradientButton extends JButton {

    public GradientButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.BLACK);
        setFont(new Font("Segoe UI", Font.BOLD, 16));
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(
                0, 0, new Color(210, 200, 255),
                getWidth(), getHeight(), new Color(170, 160, 240)
        );

        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

        g2.dispose();
        super.paintComponent(g);
    }
}