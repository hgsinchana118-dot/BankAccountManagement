import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class BankGUI extends JFrame {

    private Bank bank = new Bank();

    // ── Palette ────────────────────────────────────────────────────────────────
    static final Color BG_DEEP      = new Color(10,  14,  26);
    static final Color BG_PANEL     = new Color(16,  22,  40);
    static final Color BG_CARD      = new Color(22,  30,  54);
    static final Color BG_INPUT     = new Color(28,  38,  66);
    static final Color ACCENT_BLUE  = new Color(64,  156, 255);
    static final Color ACCENT_TEAL  = new Color(0,   210, 190);
    static final Color ACCENT_GOLD  = new Color(255, 196,  0);
    static final Color TEXT_PRIMARY = new Color(230, 235, 255);
    static final Color TEXT_MUTED   = new Color(110, 125, 165);
    static final Color SUCCESS      = new Color(0,   210, 120);
    static final Color DANGER       = new Color(255,  75,  75);
    static final Color BORDER       = new Color(40,  55,  95);

    // ── Fonts ──────────────────────────────────────────────────────────────────
    static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    static final Font FONT_HEAD   = new Font("Segoe UI", Font.BOLD,  15);
    static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 12);
    static final Font FONT_BIG    = new Font("Segoe UI", Font.BOLD,  30);

    // ── State ──────────────────────────────────────────────────────────────────
    private CardLayout   cardLayout;
    private JPanel       contentPanel;
    private JLabel       statAccounts, statBalance;
    private DefaultTableModel tableModel;
    private JTextArea    logArea;
    private JButton      activeNav;

    public BankGUI() {
        setTitle("NexBank — Account Management");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1060, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setUndecorated(false);
        getContentPane().setBackground(BG_DEEP);
        setLayout(new BorderLayout(0, 0));

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        refreshStats();
        refreshTable();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel side = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_PANEL, 0, getHeight(), new Color(12,18,35));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border glow line
                g2.setColor(new Color(64,156,255,60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
            }
        };
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo block
        JPanel logoBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 22));
        logoBlock.setOpaque(false);
        logoBlock.setMaximumSize(new Dimension(220, 72));

        // Circle icon
        JLabel icon = new JLabel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, 36, 36, ACCENT_TEAL);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String t = "N";
                g2.drawString(t, (36 - fm.stringWidth(t)) / 2, (36 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        icon.setPreferredSize(new Dimension(36, 36));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        JLabel appName = new JLabel("NexBank");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appName.setForeground(TEXT_PRIMARY);
        JLabel appSub = new JLabel("Management System");
        appSub.setFont(FONT_SMALL);
        appSub.setForeground(TEXT_MUTED);
        namePanel.add(appName);
        namePanel.add(appSub);

        logoBlock.add(icon);
        logoBlock.add(namePanel);
        side.add(logoBlock);

        // Divider
        side.add(makeDivider());
        side.add(Box.createVerticalStrut(10));

        // Nav label
        JLabel navLabel = new JLabel("  NAVIGATION");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navLabel.setForeground(TEXT_MUTED);
        navLabel.setAlignmentX(LEFT_ALIGNMENT);
        navLabel.setBorder(new EmptyBorder(4, 20, 6, 0));
        navLabel.setMaximumSize(new Dimension(220, 24));
        side.add(navLabel);

        String[][] navItems = {
            {"\u25A0  Dashboard",     "DASHBOARD"},
            {"\u25A0  Create Account","CREATE"},
            {"\u25A0  Deposit",       "DEPOSIT"},
            {"\u25A0  Withdraw",      "WITHDRAW"},
            {"\u25A0  Check Balance", "CHECK"},
            {"\u25A0  All Accounts",  "ACCOUNTS"},
        };
        JButton first = null;
        for (String[] nav : navItems) {
            JButton btn = navButton(nav[0], nav[1]);
            if (first == null) first = btn;
            side.add(btn);
            side.add(Box.createVerticalStrut(2));
        }

        side.add(Box.createVerticalGlue());

        // Bottom user card
        side.add(makeDivider());
        JPanel user = userCard();
        side.add(user);

        // Set first active
        if (first != null) setActiveNav(first);
        return side;
    }

    private JButton navButton(String label, String card) {
        JButton btn = new JButton(label) {
            boolean active = false;
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getClientProperty("active") == Boolean.TRUE) {
                    // Active pill bg
                    g2.setColor(new Color(64, 156, 255, 28));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 10, 10);
                    // Left accent bar
                    GradientPaint bar = new GradientPaint(0,0, ACCENT_BLUE, 0, getHeight(), ACCENT_TEAL);
                    g2.setPaint(bar);
                    g2.fillRoundRect(8, 4, 3, getHeight()-8, 3, 3);
                    g2.setColor(TEXT_PRIMARY);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,10));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 10, 10);
                    g2.setColor(new Color(180, 200, 240));
                } else {
                    g2.setColor(TEXT_MUTED);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 28, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BODY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, card);
            setActiveNav(btn);
        });
        return btn;
    }

    private void setActiveNav(JButton btn) {
        if (activeNav != null) {
            activeNav.putClientProperty("active", Boolean.FALSE);
            activeNav.repaint();
        }
        activeNav = btn;
        btn.putClientProperty("active", Boolean.TRUE);
        btn.repaint();
    }

    private JPanel userCard() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(220, 62));

        JLabel avatar = new JLabel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                LinearGradientPaint gp = new LinearGradientPaint(0,0,220,0, new float[]{0f,0.5f,1f}, new Color[]{new Color(0,0,0,0), BORDER, new Color(0,0,0,0)});
                g2.setPaint(gp);
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String t = "AD";
                g2.drawString(t, (32 - fm.stringWidth(t))/2, (32 + fm.getAscent() - fm.getDescent())/2);
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel name = new JLabel("Admin");
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TEXT_PRIMARY);
        JLabel role = new JLabel("Bank Manager");
        role.setFont(FONT_SMALL);
        role.setForeground(TEXT_MUTED);
        info.add(name); info.add(role);
        p.add(avatar); p.add(info);
        return p;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
               LinearGradientPaint gp = new LinearGradientPaint(0,0,220,0, new float[]{0f,0.5f,1f}, new Color[]{new Color(0,0,0,0), BORDER, new Color(0,0,0,0)});
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 1);
            }
        };
        sep.setMaximumSize(new Dimension(220, 1));
        return sep;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CONTENT AREA
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.add(buildDashboard(), "DASHBOARD");
        contentPanel.add(buildCreate(),    "CREATE");
        contentPanel.add(buildDeposit(),   "DEPOSIT");
        contentPanel.add(buildWithdraw(),  "WITHDRAW");
        contentPanel.add(buildCheck(),     "CHECK");
        contentPanel.add(buildAccounts(),  "ACCOUNTS");
        cardLayout.show(contentPanel, "DASHBOARD");
        return contentPanel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel p = darkPage();

        // Header
        p.add(pageHeader("Dashboard", "Welcome back, Admin"), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Stat cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(9999, 110));

        statAccounts = bigStatLabel("0");
        statBalance  = bigStatLabel("Rs. 0.00");
        JLabel statTx = bigStatLabel("0");

        statsRow.add(glowCard("Total Accounts", statAccounts, ACCENT_BLUE,  "\u25CF"));
        statsRow.add(glowCard("Total Balance",  statBalance,  ACCENT_TEAL,  "\u25CF"));
        statsRow.add(glowCard("Transactions",   statTx,       ACCENT_GOLD,  "\u25CF"));

        // Log panel
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(BG_CARD);
        logArea.setForeground(new Color(100, 220, 160));
        logArea.setFont(FONT_MONO);
        logArea.setBorder(new EmptyBorder(14, 16, 14, 16));
        logArea.setText("[ NexBank System Initialized ]\n[ Awaiting transactions... ]\n");

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(0,0,0,0)
        ));
        logScroll.setOpaque(false);

        JLabel logTitle = sectionLabel("Live Transaction Log");

        center.add(statsRow);
        center.add(Box.createVerticalStrut(22));
        center.add(logTitle);
        center.add(Box.createVerticalStrut(8));
        center.add(logScroll);
        center.add(Box.createVerticalGlue());

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel glowCard(String label, JLabel valueLabel, Color accent, String dot) {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Top accent bar
               LinearGradientPaint bar = new LinearGradientPaint(0,0,getWidth(),0, new float[]{0f,0.5f,1f}, new Color[]{accent, accent.darker(), new Color(0,0,0,0)});
                g2.setPaint(bar);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                // Border
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);

        JLabel dotLbl = new JLabel(dot);
        dotLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dotLbl.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lbl, BorderLayout.WEST);
        top.add(dotLbl, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel bigStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BIG);
        l.setForeground(TEXT_PRIMARY);
        l.setBorder(new EmptyBorder(6, 0, 0, 0));
        return l;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CREATE ACCOUNT
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildCreate() {
        JPanel p = darkPage();
        p.add(pageHeader("Create Account", "Register a new bank customer"), BorderLayout.NORTH);

        JPanel form = formCard();
        JTextField nameField   = darkField("Full Name", "e.g. Sinchana HG");
        JTextField depositField = darkField("Initial Deposit", "e.g. 5000");
        JLabel     res          = resultLbl();
        JButton    btn          = gradientButton("Create Account", ACCENT_BLUE, ACCENT_TEAL);

        btn.addActionListener(e -> {
            String name   = nameField.getText().trim();
            String depStr = depositField.getText().trim();
            if (name.isEmpty() || depStr.isEmpty()) {
                setResult(res, "Please fill in all fields.", false); return;
            }
            try {
                double dep = Double.parseDouble(depStr);
                String msg = bank.createAccount(name, dep);
                boolean ok = msg.startsWith("SUCCESS");
                setResult(res, ok ? "Account created successfully!" : msg, ok);
                if (ok) { nameField.setText(""); depositField.setText(""); refreshStats(); refreshTable(); appendLog(msg); }
            } catch (NumberFormatException ex) {
                setResult(res, "Enter a valid number for deposit.", false);
            }
        });

        addRow(form, "Holder Name",            nameField);
        addRow(form, "Initial Deposit (Rs.)",  depositField);
        form.add(Box.createVerticalStrut(10));
        form.add(btn);
        form.add(Box.createVerticalStrut(12));
        form.add(res);
        form.add(Box.createVerticalGlue());

        p.add(centeredForm(form), BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DEPOSIT
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildDeposit() {
        JPanel p = darkPage();
        p.add(pageHeader("Deposit Funds", "Add money to a customer account"), BorderLayout.NORTH);

        JPanel form = formCard();
        JTextField accField = darkField("Account Number", "e.g. ACC1001");
        JTextField amtField = darkField("Amount", "e.g. 2000");
        JLabel res  = resultLbl();
        JButton btn = gradientButton("Deposit Funds", SUCCESS, new Color(0,180,100));

        btn.addActionListener(e -> {
            String accNo = accField.getText().trim().toUpperCase();
            BankAccount acc = bank.getAccount(accNo);
            if (acc == null) { setResult(res, "Account " + accNo + " not found.", false); return; }
            try {
                String msg = acc.deposit(Double.parseDouble(amtField.getText().trim()));
                boolean ok = msg.startsWith("SUCCESS");
                setResult(res, ok ? "Deposit successful! " + msg.replaceFirst("SUCCESS: ","") : msg, ok);
                if (ok) { amtField.setText(""); refreshStats(); refreshTable(); appendLog("["+accNo+"] " + msg); }
            } catch (NumberFormatException ex) { setResult(res, "Enter a valid amount.", false); }
        });

        addRow(form, "Account Number", accField);
        addRow(form, "Amount (Rs.)",   amtField);
        form.add(Box.createVerticalStrut(10)); form.add(btn);
        form.add(Box.createVerticalStrut(12)); form.add(res);
        form.add(Box.createVerticalGlue());

        p.add(centeredForm(form), BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  WITHDRAW
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildWithdraw() {
        JPanel p = darkPage();
        p.add(pageHeader("Withdraw Funds", "Debit from a customer account"), BorderLayout.NORTH);

        JPanel form = formCard();
        JTextField accField = darkField("Account Number", "e.g. ACC1001");
        JTextField amtField = darkField("Amount", "e.g. 1000");
        JLabel res  = resultLbl();
        JButton btn = gradientButton("Withdraw Funds", DANGER, new Color(200, 40, 40));

        btn.addActionListener(e -> {
            String accNo = accField.getText().trim().toUpperCase();
            BankAccount acc = bank.getAccount(accNo);
            if (acc == null) { setResult(res, "Account " + accNo + " not found.", false); return; }
            try {
                String msg = acc.withdraw(Double.parseDouble(amtField.getText().trim()));
                boolean ok = msg.startsWith("SUCCESS");
                setResult(res, ok ? "Withdrawal successful! " + msg.replaceFirst("SUCCESS: ","") : msg, ok);
                if (ok) { amtField.setText(""); refreshStats(); refreshTable(); appendLog("["+accNo+"] " + msg); }
            } catch (NumberFormatException ex) { setResult(res, "Enter a valid amount.", false); }
        });

        addRow(form, "Account Number", accField);
        addRow(form, "Amount (Rs.)",   amtField);
        form.add(Box.createVerticalStrut(10)); form.add(btn);
        form.add(Box.createVerticalStrut(12)); form.add(res);
        form.add(Box.createVerticalGlue());

        p.add(centeredForm(form), BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHECK BALANCE
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildCheck() {
        JPanel p = darkPage();
        p.add(pageHeader("Check Balance", "View account balance and details"), BorderLayout.NORTH);

        JPanel form = formCard();
        JTextField accField = darkField("Account Number", "e.g. ACC1001");
        JLabel res  = resultLbl();

        // Info display card
        JPanel infoCard = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 200, 160, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0, 200, 160, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        infoCard.setOpaque(false);
        infoCard.setLayout(new GridLayout(3, 2, 10, 8));
        infoCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        infoCard.setVisible(false);
        infoCard.setMaximumSize(new Dimension(9999, 100));
        infoCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblAccNo  = infoKV("Account No.", "—");
        JLabel lblName   = infoKV("Holder",      "—");
        JLabel lblBal    = infoKV("Balance",      "—");
        JLabel lblAccNo2 = infoKV("Status",       "Active");
        infoCard.add(new JLabel("Account No."){ { setFont(FONT_SMALL); setForeground(TEXT_MUTED); } });
        infoCard.add(lblAccNo);
        infoCard.add(new JLabel("Holder"){ { setFont(FONT_SMALL); setForeground(TEXT_MUTED); } });
        infoCard.add(lblName);
        infoCard.add(new JLabel("Balance"){ { setFont(FONT_SMALL); setForeground(TEXT_MUTED); } });
        infoCard.add(lblBal);

        JButton btn = gradientButton("Check Balance", ACCENT_TEAL, new Color(0,140,180));
        btn.addActionListener(e -> {
            String accNo = accField.getText().trim().toUpperCase();
            BankAccount acc = bank.getAccount(accNo);
            if (acc == null) {
                setResult(res, "Account " + accNo + " not found.", false);
                infoCard.setVisible(false);
            } else {
                setResult(res, "", true);
                lblAccNo.setText(acc.getAccountNumber());
                lblName.setText(acc.getHolderName());
                lblBal.setText(String.format("Rs. %.2f", acc.getBalance()));
                infoCard.setVisible(true);
                appendLog("Balance check: " + acc.getAccountNumber() + " = Rs." + String.format("%.2f", acc.getBalance()));
            }
        });

        addRow(form, "Account Number", accField);
        form.add(Box.createVerticalStrut(10)); form.add(btn);
        form.add(Box.createVerticalStrut(12)); form.add(res);
        form.add(Box.createVerticalStrut(8));  form.add(infoCard);
        form.add(Box.createVerticalGlue());

        p.add(centeredForm(form), BorderLayout.CENTER);
        return p;
    }

    private JLabel infoKV(String key, String value) {
        JLabel l = new JLabel(value);
        l.setFont(FONT_HEAD);
        l.setForeground(ACCENT_TEAL);
        return l;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ALL ACCOUNTS
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildAccounts() {
        JPanel p = darkPage();
        p.add(pageHeader("All Accounts", "Full list of registered accounts"), BorderLayout.NORTH);

        String[] cols = {"Account Number", "Holder Name", "Balance (Rs.)"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? BG_CARD : new Color(26, 36, 60));
                c.setForeground(TEXT_PRIMARY);
                if (isRowSelected(row)) c.setBackground(new Color(64, 156, 255, 80));
                return c;
            }
        };
        table.setFont(FONT_BODY);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 2));
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(64, 156, 255, 80));
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(18, 26, 50));
        header.setForeground(TEXT_MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 42));

        // Cell padding
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                setBackground(row % 2 == 0 ? BG_CARD : new Color(26, 36, 60));
                setForeground(col == 2 ? ACCENT_TEAL : TEXT_PRIMARY);
                if (sel) setBackground(new Color(64, 156, 255, 50));
                return this;
            }
        };
        for (int i = 0; i < 3; i++) table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_CARD);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JButton refresh = gradientButton("Refresh Table", ACCENT_BLUE, new Color(50, 100, 220));
        refresh.addActionListener(e -> refreshTable());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        btnRow.setOpaque(false);
        btnRow.add(refresh);

        p.add(scroll, BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REUSABLE HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel darkPage() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(BG_DEEP);
        p.setBorder(new EmptyBorder(28, 32, 28, 32));
        return p;
    }

    private JPanel pageHeader(String title, String subtitle) {
        JPanel h = new JPanel();
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(0, 0, 22, 0));

        JLabel t = new JLabel(title);
        t.setFont(FONT_TITLE);
        t.setForeground(TEXT_PRIMARY);

        JLabel s = new JLabel(subtitle);
        s.setFont(FONT_BODY);
        s.setForeground(TEXT_MUTED);

        // Underline accent
        JPanel underline = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                LinearGradientPaint gp = new LinearGradientPaint(0,0,200,0, new float[]{0f,0.4f,1f}, new Color[]{ACCENT_BLUE, ACCENT_TEAL, new Color(0,0,0,0)});
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
                g2.dispose();
            }
        };
        underline.setOpaque(false);
        underline.setMaximumSize(new Dimension(9999, 2));
        underline.setPreferredSize(new Dimension(200, 2));

        h.add(t);
        h.add(Box.createVerticalStrut(3));
        h.add(s);
        h.add(Box.createVerticalStrut(10));
        h.add(underline);
        return h;
    }

    private JPanel formCard() {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(26, 28, 26, 28));
        return card;
    }

    private JPanel centeredForm(JPanel form) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.setMaximumSize(new Dimension(460, 9999));
        form.setPreferredSize(new Dimension(420, 340));
        wrap.add(form, gbc);
        return wrap;
    }

    private JTextField darkField(String name, String placeholder) {
        JTextField f = new JTextField(22) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner() ? ACCENT_BLUE : BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_BLUE);
        f.setBackground(new Color(0,0,0,0));
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(10, 14, 10, 14));
        f.setMaximumSize(new Dimension(9999, 42));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setToolTipText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.repaint(); }
            public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private void addRow(JPanel form, String labelText, JTextField field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        field.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lbl);
        form.add(field);
        form.add(Box.createVerticalStrut(14));
    }

    private JButton gradientButton(String text, Color c1, Color c2) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = getModel().isPressed()
                    ? new GradientPaint(0,0, c1.darker(), getWidth(),0, c2.darker())
                    : new GradientPaint(0,0, c1, getWidth(),0, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(200, 42));
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel resultLbl() {
        JLabel l = new JLabel(" ");
        l.setFont(FONT_BODY);
        l.setForeground(SUCCESS);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void setResult(JLabel lbl, String msg, boolean ok) {
        lbl.setText(msg.isEmpty() ? " " : msg);
        lbl.setForeground(ok ? SUCCESS : DANGER);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void appendLog(String msg) {
        if (logArea == null) return;
        logArea.append(">> " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        // Increment transaction counter
        if (statAccounts != null) {
            // re-use dashboard refresh
        }
    }

    private void refreshStats() {
        if (statAccounts == null) return;
        statAccounts.setText(String.valueOf(bank.getAllAccounts().size()));
        statBalance.setText(String.format("Rs. %.2f", bank.getTotalBalance()));
    }

    private void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (BankAccount acc : bank.getAllAccounts()) {
            tableModel.addRow(new Object[]{
                acc.getAccountNumber(),
                acc.getHolderName(),
                String.format("%.2f", acc.getBalance())
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(BankGUI::new);
    }
}