package com.lru.project;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Lightweight Swing front end for the existing cache, simulator, and DAO. */
public class DesktopApp {
    private static final Color INK = new Color(31, 43, 61);
    private static final Color MUTED = new Color(104, 117, 135);
    private static final Color BLUE = new Color(48, 99, 184);
    private static final Color BG = new Color(245, 247, 251);
    private static final Color BORDER = new Color(224, 230, 238);
    private static final Color GREEN = new Color(31, 125, 84);
    private static final Color RED = new Color(184, 67, 67);
    private final SimulationDAO dao = new SimulationDAO();
    private final JFrame frame = new JFrame("LRU Cache & Page Simulator");
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel status = new JLabel("Ready");
    private final DefaultTableModel pageModel = model("Page", "Result", "Memory State");
    private final DefaultTableModel historyModel = model("ID", "Frames", "Reference String", "Hits", "Faults", "Hit Ratio", "Created At");
    private final JLabel hitsValue = metricValue("—");
    private final JLabel faultsValue = metricValue("—");
    private final JLabel ratioValue = metricValue("—");
    private final JLabel finalMemory = new JLabel("Run a simulation to see the final frame contents.");
    private final JTextArea cacheLog = new JTextArea(9, 42);
    private final JLabel cacheState = new JLabel("[MRU] [LRU]");
    private final JTextField cacheKey = field("e.g. A");
    private final JTextField cacheValue = field("e.g. Apple");
    private LRUCache<String, String> cache = new LRUCache<>(3);
    private final List<String> cacheHistory = new ArrayList<>();

    public void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 640));
        frame.setSize(1080, 760);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(buildShell());
        frame.setVisible(true);
        initializeDatabase();
    }

    private JPanel buildShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(BG);
        shell.add(topBar(), BorderLayout.NORTH);
        content.setBackground(BG);
        content.add(homeView(), "home");
        content.add(cacheView(), "cache");
        content.add(simulatorView(), "simulator");
        content.add(historyView(), "history");
        shell.add(content, BorderLayout.CENTER);
        status.setForeground(MUTED);
        status.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(9, 24, 9, 24)));
        shell.add(status, BorderLayout.SOUTH);
        return shell;
    }

    private JPanel topBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        JLabel brand = new JLabel("LRU  /  ALGORITHM LAB");
        brand.setFont(new Font("SansSerif", Font.BOLD, 14));
        brand.setForeground(INK);
        JButton home = button("Home", false);
        home.addActionListener(e -> cards.show(content, "home"));
        top.add(brand, BorderLayout.WEST);
        top.add(home, BorderLayout.EAST);
        return top;
    }

    private JPanel homeView() {
        JPanel page = pagePanel();
        JPanel wrap = new JPanel(new BorderLayout(0, 25));
        wrap.setOpaque(false);
        wrap.add(header("LRU Cache & Page Simulator", "Algorithm Visualization Tool"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 14, 14);
        addTile(grid, c, 0, 0, "LRU Cache", "Explore key-value operations and recent-use order.", "Open cache demo", () -> cards.show(content, "cache"));
        c.insets = new Insets(0, 0, 14, 0);
        addTile(grid, c, 1, 0, "Page Simulator", "Step through page hits, faults, and frame states.", "Run simulation", () -> cards.show(content, "simulator"));
        c.insets = new Insets(0, 0, 0, 14);
        addTile(grid, c, 0, 1, "Simulation History", "Review runs saved in your MySQL database.", "View history", () -> { cards.show(content, "history"); refreshHistory(); });
        c.insets = new Insets(0, 0, 0, 0);
        addTile(grid, c, 1, 1, "Clear History", "Remove saved simulation runs.", "Clear saved runs", this::confirmClearHistory);
        wrap.add(grid, BorderLayout.CENTER);
        page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    private void addTile(JPanel grid, GridBagConstraints c, int x, int y, String title, String description, String action, Runnable run) {
        JPanel tile = cardPanel();
        tile.setLayout(new BorderLayout(0, 12));
        tile.add(heading(title, 19), BorderLayout.NORTH);
        tile.add(body(description), BorderLayout.CENTER);
        JButton b = button(action, true);
        b.addActionListener(e -> run.run());
        tile.add(b, BorderLayout.SOUTH);
        c.gridx = x; c.gridy = y;
        grid.add(tile, c);
    }

    private JPanel cacheView() {
        JPanel page = pagePanel();
        JPanel wrap = new JPanel(new BorderLayout(0, 18));
        wrap.setOpaque(false);
        wrap.add(header("LRU Cache Demo", "Capacity is fixed at 3 entries. Recently used items move to the front."), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        JPanel form = cardPanel();
        form.setLayout(new BorderLayout(0, 12));
        JPanel inputs = new JPanel(new GridBagLayout()); inputs.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(0, 0, 8, 10); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; inputs.add(label("Key"), c); c.gridx = 1; inputs.add(cacheKey, c);
        c.gridx = 2; inputs.add(label("Value"), c); c.gridx = 3; c.insets = new Insets(0, 0, 8, 0); inputs.add(cacheValue, c);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); actions.setOpaque(false);
        JButton put = button("Put / Update", true); put.addActionListener(e -> cachePut());
        JButton get = button("Get key", false); get.addActionListener(e -> cacheGet());
        JButton reset = button("Reset demo", false); reset.addActionListener(e -> resetCache());
        actions.add(put); actions.add(get); actions.add(reset);
        form.add(heading("Cache capacity: 3", 16), BorderLayout.NORTH); form.add(inputs, BorderLayout.CENTER); form.add(actions, BorderLayout.SOUTH);
        JPanel state = cardPanel(); state.setLayout(new BorderLayout(0, 10));
        state.add(heading("Current cache state", 17), BorderLayout.NORTH);
        cacheState.setFont(new Font("Monospaced", Font.BOLD, 17)); cacheState.setForeground(BLUE);
        state.add(cacheState, BorderLayout.CENTER);
        JPanel logPanel = cardPanel(); logPanel.setLayout(new BorderLayout(0, 9));
        logPanel.add(heading("Operations", 17), BorderLayout.NORTH);
        cacheLog.setEditable(false); cacheLog.setFont(new Font("Monospaced", Font.PLAIN, 13)); cacheLog.setBackground(new Color(250, 251, 253));
        logPanel.add(new JScrollPane(cacheLog), BorderLayout.CENTER);
        body.add(form, BorderLayout.NORTH);
        JPanel lower = new JPanel(new BorderLayout(0, 14)); lower.setOpaque(false); lower.add(state, BorderLayout.NORTH); lower.add(logPanel, BorderLayout.CENTER);
        body.add(lower, BorderLayout.CENTER);
        wrap.add(body, BorderLayout.CENTER);
        page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    private JPanel simulatorView() {
        JPanel page = pagePanel();
        JPanel wrap = new JPanel(new BorderLayout(0, 16)); wrap.setOpaque(false);
        wrap.add(header("Page Replacement Simulator", "Enter a frame count and a space or comma separated page reference string."), BorderLayout.NORTH);
        JPanel all = new JPanel(new BorderLayout(0, 14)); all.setOpaque(false);
        JPanel form = cardPanel(); form.setLayout(new BorderLayout(0, 10));
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        JTextField frames = field("3"); frames.setText("3");
        JTextField references = field("7 0 1 2 0 3 0 4");
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(0, 0, 0, 12); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; fields.add(label("Frames"), c); c.gridx = 1; c.weightx = .25; c.fill = GridBagConstraints.HORIZONTAL; fields.add(frames, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE; fields.add(label("Page references"), c); c.gridx = 3; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; fields.add(references, c);
        JButton run = button("Run Simulation", true);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); right.setOpaque(false); right.add(run);
        form.add(fields, BorderLayout.CENTER); form.add(right, BorderLayout.SOUTH);
        JPanel metrics = new JPanel(new GridBagLayout()); metrics.setOpaque(false);
        addMetric(metrics, 0, "TOTAL HITS", hitsValue); addMetric(metrics, 1, "TOTAL FAULTS", faultsValue); addMetric(metrics, 2, "HIT RATIO", ratioValue);
        JPanel result = cardPanel(); result.setLayout(new BorderLayout(0, 10));
        JPanel resultHead = new JPanel(new BorderLayout()); resultHead.setOpaque(false); resultHead.add(heading("Page-by-page results", 17), BorderLayout.WEST); resultHead.add(metrics, BorderLayout.EAST);
        JTable table = new JTable(pageModel); styleTable(table); table.getColumnModel().getColumn(1).setCellRenderer(new ResultRenderer());
        result.add(resultHead, BorderLayout.NORTH); result.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel finalPanel = new JPanel(new BorderLayout(8, 0)); finalPanel.setOpaque(false);
        finalPanel.add(heading("Final memory state", 15), BorderLayout.WEST); finalMemory.setForeground(BLUE); finalMemory.setFont(new Font("Monospaced", Font.BOLD, 13)); finalPanel.add(finalMemory, BorderLayout.CENTER);
        result.add(finalPanel, BorderLayout.SOUTH);
        all.add(form, BorderLayout.NORTH); all.add(result, BorderLayout.CENTER);
        wrap.add(all, BorderLayout.CENTER); page.add(wrap, BorderLayout.CENTER);
        run.addActionListener(e -> runSimulation(frames.getText(), references.getText(), run));
        return page;
    }

    private JPanel historyView() {
        JPanel page = pagePanel();
        JPanel wrap = new JPanel(new BorderLayout(0, 16)); wrap.setOpaque(false);
        JPanel titleRow = new JPanel(new BorderLayout()); titleRow.setOpaque(false);
        titleRow.add(header("Simulation History", "Runs are read from the existing MySQL simulation_runs table."), BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)); actions.setOpaque(false);
        JButton refresh = button("Refresh", false); refresh.addActionListener(e -> refreshHistory());
        JButton clear = button("Clear History", false); clear.addActionListener(e -> confirmClearHistory());
        actions.add(refresh); actions.add(clear); titleRow.add(actions, BorderLayout.EAST);
        JPanel tableCard = cardPanel(); tableCard.setLayout(new BorderLayout(0, 10));
        JTable table = new JTable(historyModel); styleTable(table); table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {55, 65, 300, 60, 65, 85, 165};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        wrap.add(titleRow, BorderLayout.NORTH); wrap.add(tableCard, BorderLayout.CENTER); page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    private void runSimulation(String frameText, String referenceText, JButton button) {
        final int frameCount;
        final int[] pages;
        try {
            frameCount = Integer.parseInt(frameText.trim());
            if (frameCount <= 0) throw new IllegalArgumentException("Frames must be greater than zero.");
            String trimmed = referenceText.trim();
            if (trimmed.isEmpty()) throw new IllegalArgumentException("Enter at least one page reference.");
            String[] parts = trimmed.split("[\\s,]+");
            if (parts.length > 100) throw new IllegalArgumentException("Enter no more than 100 page references.");
            pages = new int[parts.length];
            for (int i = 0; i < parts.length; i++) pages[i] = Integer.parseInt(parts[i]);
            if (String.join(" ", parts).length() > 500) throw new IllegalArgumentException("Reference string is too long to save (maximum 500 characters).");
        } catch (NumberFormatException e) { showError("Use whole numbers for frames and page references."); return; }
        catch (IllegalArgumentException e) { showError(e.getMessage()); return; }
        pageModel.setRowCount(0); hitsValue.setText("…"); faultsValue.setText("…"); ratioValue.setText("…"); finalMemory.setText("Simulation running…");
        setBusy(button, true, "Running simulation…");
        new SwingWorker<SimulationOutcome, Void>() {
            protected SimulationOutcome doInBackground() {
                List<PageReplacementSimulator.PageStep> steps = new ArrayList<>();
                SimulationDAO.SimulationResult result = PageReplacementSimulator.simulate(pages, frameCount, steps::add);
                dao.saveChecked(result);
                return new SimulationOutcome(result, steps);
            }
            protected void done() {
                setBusy(button, false, "Run Simulation");
                try {
                    SimulationOutcome outcome = get();
                    for (PageReplacementSimulator.PageStep step : outcome.steps()) pageModel.addRow(new Object[] { step.page(), step.result(), step.memoryState() });
                    SimulationDAO.SimulationResult r = outcome.result();
                    hitsValue.setText(String.valueOf(r.hits())); faultsValue.setText(String.valueOf(r.faults())); ratioValue.setText(String.format("%.2f%%", r.hitRatio()));
                    finalMemory.setText(outcome.steps().get(outcome.steps().size() - 1).memoryState());
                    setStatus("Simulation completed and saved to history.", GREEN);
                } catch (Exception e) { finalMemory.setText("Simulation could not be completed."); showError(message(e)); }
            }
        }.execute();
    }

    private record SimulationOutcome(SimulationDAO.SimulationResult result, List<PageReplacementSimulator.PageStep> steps) { }

    private void refreshHistory() {
        setStatus("Loading history…", MUTED);
        new SwingWorker<List<SimulationDAO.HistoryEntry>, Void>() {
            protected List<SimulationDAO.HistoryEntry> doInBackground() { return dao.findHistoryChecked(); }
            protected void done() {
                try {
                    historyModel.setRowCount(0);
                    for (SimulationDAO.HistoryEntry entry : get()) {
                        SimulationDAO.SimulationResult r = entry.result();
                        historyModel.addRow(new Object[] { entry.id(), r.frames(), r.referenceString(), r.hits(), r.faults(), String.format("%.2f%%", r.hitRatio()), r.createdAt() });
                    }
                    setStatus(historyModel.getRowCount() == 0 ? "No saved simulations yet." : "History refreshed.", MUTED);
                } catch (Exception e) { showError(message(e)); }
            }
        }.execute();
    }

    private void confirmClearHistory() {
        int choice = JOptionPane.showConfirmDialog(frame, "Delete all saved simulation runs? This cannot be undone.", "Clear simulation history", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        setStatus("Clearing saved runs…", MUTED);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() { dao.deleteAllChecked(); return null; }
            protected void done() {
                try { get(); historyModel.setRowCount(0); setStatus("Simulation history cleared.", GREEN); }
                catch (Exception e) { showError(message(e)); }
            }
        }.execute();
    }

    private void initializeDatabase() {
        setStatus("Connecting to MySQL…", MUTED);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() { DatabaseManager.init(); return null; }
            protected void done() {
                try { get(); setStatus("Connected. Ready to explore.", GREEN); }
                catch (Exception e) { showError("Could not initialize the MySQL database. Check the server and connection settings in DatabaseManager.java.\n\n" + message(e)); }
            }
        }.execute();
    }

    private void cachePut() {
        String key = cacheKey.getText().trim(), value = cacheValue.getText().trim();
        if (key.isEmpty() || value.isEmpty()) { showError("Enter both a key and a value."); return; }
        boolean existed = cache.containsKey(key);
        cache.put(key, value);
        cacheHistory.add((existed ? "Updated " : "Put ") + key + " = " + value);
        refreshCache(); setStatus("Cache updated.", GREEN);
    }

    private void cacheGet() {
        String key = cacheKey.getText().trim();
        if (key.isEmpty()) { showError("Enter a key to look up."); return; }
        String value = cache.get(key);
        cacheHistory.add(value == null ? "Miss: " + key + " is not in the cache." : "Hit: " + key + " = " + value);
        refreshCache(); setStatus(value == null ? "Cache miss." : "Cache hit.", value == null ? MUTED : GREEN);
    }

    private void resetCache() { cache = new LRUCache<>(3); cacheHistory.clear(); refreshCache(); setStatus("Cache demo reset.", MUTED); }
    private void refreshCache() { cacheState.setText(cache.toString()); cacheLog.setText(String.join("\n", cacheHistory)); cacheLog.setCaretPosition(cacheLog.getDocument().getLength()); }

    private JPanel pagePanel() { JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG); p.setBorder(BorderFactory.createEmptyBorder(28, 34, 26, 34)); return p; }
    private JPanel cardPanel() { JPanel p = new JPanel(); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(18, 20, 18, 20))); return p; }
    private JPanel header(String title, String subtitle) { JPanel p = new JPanel(new BorderLayout(0, 6)); p.setOpaque(false); p.add(heading(title, 26), BorderLayout.NORTH); p.add(body(subtitle), BorderLayout.CENTER); return p; }
    private JLabel heading(String text, int size) { JLabel l = new JLabel(text); l.setFont(new Font("SansSerif", Font.BOLD, size)); l.setForeground(INK); return l; }
    private JLabel body(String text) { JLabel l = new JLabel(text); l.setFont(new Font("SansSerif", Font.PLAIN, 14)); l.setForeground(MUTED); return l; }
    private JLabel label(String text) { JLabel l = new JLabel(text); l.setFont(new Font("SansSerif", Font.BOLD, 13)); l.setForeground(INK); return l; }
    private JLabel metricValue(String text) { JLabel l = new JLabel(text, SwingConstants.RIGHT); l.setFont(new Font("SansSerif", Font.BOLD, 18)); l.setForeground(BLUE); return l; }
    private JTextField field(String hint) { JTextField f = new JTextField(18); f.setToolTipText(hint); f.setFont(new Font("SansSerif", Font.PLAIN, 14)); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(8, 10, 8, 10))); return f; }
    private JButton button(String text, boolean primary) { JButton b = new JButton(text); b.setFont(new Font("SansSerif", Font.BOLD, 13)); b.setFocusPainted(false); b.setOpaque(true); b.setBackground(primary ? BLUE : Color.WHITE); b.setForeground(primary ? Color.WHITE : INK); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? BLUE : BORDER), BorderFactory.createEmptyBorder(8, 14, 8, 14))); return b; }
    private DefaultTableModel model(String... columns) { return new DefaultTableModel(columns, 0) { public boolean isCellEditable(int row, int column) { return false; } }; }
    private void styleTable(JTable table) { table.setRowHeight(30); table.setFont(new Font("SansSerif", Font.PLAIN, 13)); table.setForeground(INK); table.setGridColor(BORDER); table.setShowVerticalLines(false); table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12)); table.getTableHeader().setBackground(new Color(243, 246, 250)); table.getTableHeader().setForeground(INK); }
    private void addMetric(JPanel panel, int x, String name, JLabel value) { GridBagConstraints c = new GridBagConstraints(); c.gridx = x; c.insets = new Insets(0, 10, 0, 0); JPanel p = new JPanel(new BorderLayout(6, 0)); p.setOpaque(false); JLabel n = new JLabel(name); n.setFont(new Font("SansSerif", Font.BOLD, 10)); n.setForeground(MUTED); p.add(n, BorderLayout.NORTH); p.add(value, BorderLayout.SOUTH); panel.add(p, c); }
    private void setBusy(JButton button, boolean busy, String text) { button.setEnabled(!busy); button.setText(text); setStatus(busy ? "Working…" : "Ready", MUTED); }
    private void setStatus(String text, Color color) { status.setText(text); status.setForeground(color); }
    private void showError(String text) { setStatus(text.replace('\n', ' '), RED); JOptionPane.showMessageDialog(frame, text, "Unable to complete", JOptionPane.ERROR_MESSAGE); }
    private String message(Exception e) { Throwable cause = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e; String m = cause.getMessage(); return m == null || m.isBlank() ? "An unexpected error occurred." : m; }

    private static class ResultRenderer extends javax.swing.table.DefaultTableCellRenderer {
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            if (!selected) { String result = String.valueOf(value); c.setForeground("HIT".equals(result) ? GREEN : RED); }
            setFont(getFont().deriveFont(Font.BOLD)); setHorizontalAlignment(SwingConstants.CENTER); return c;
        }
    }
}
