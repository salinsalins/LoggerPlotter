package binp.nbi.tango.adc;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.jfree.data.xy.XYSeries;

public class LoggerPlotter extends WindowAdapter {

    //private static final Logger logger = LogManager.getLogger(LoggerPlotter.class);
    private static final Logger logger = Logger.getLogger(LoggerPlotter.class.getName());

    public static final String version = "3.0";

    private File fileLog;
    private File fileZip;
    //String[] dirList;

    String folder = ".\\";

    DirWatcher timerTask = null;
    Timer timer = new Timer();

    static LoggerPlotter window;
    private JFrame frame;
    JTextField txtFileName;
    LogViewTable logViewTable;
    JPanel panel;
    JLabel lblZipFileName;

    JTextArea txtarIncludedColumns;
    JTextArea txtarExcludedColumns;

    Color freshColor = new Color(255, 255, 255);
    Color dimmedColor = new Color(0, 255, 0);
    JCheckBox chckbxAdjustForToday;
    JCheckBox chckbxShowMarkers;
    JCheckBox chckbxShowPreviousShot;
    JCheckBox chckbxShowHyst;

    /**
     * Launch the application.
     *
     * @param args
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    window = new LoggerPlotter();
                    window.frame.setVisible(true);
                    logger.log(Level.INFO, "LoggerPlotter " + version + " started.");
                } catch (Exception e) {
                    //log.error("LoggerPlotter start error ", e);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application. Default constructor.
     */
    public LoggerPlotter() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        //frame.setResizable(false);
        frame.setBounds(100, 100, 600, 600);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(this);
        frame.getContentPane().setLayout(new CardLayout(0, 0));

        // Tabbed pane for Plot, ErorrList, Log, etc..
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        frame.getContentPane().add(tabbedPane, "name_5266296004328937");

    // Signals and Log Tab		
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.9);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        tabbedPane.addTab("Signals and Log", null, splitPane, null);

        JScrollPane scrollPane = new JScrollPane();
        splitPane.setRightComponent(scrollPane);

        logViewTable = new LogViewTable();
        scrollPane.setViewportView(logViewTable);

        JScrollPane scrollPane_1 = new JScrollPane();
        splitPane.setLeftComponent(scrollPane_1);

        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 3, 5, 5));
        scrollPane_1.setViewportView(panel);

        lblZipFileName = new JLabel("File :");
        lblZipFileName.setHorizontalAlignment(SwingConstants.CENTER);
        lblZipFileName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        scrollPane_1.setColumnHeaderView(lblZipFileName);

        splitPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent arg0) {
                //System.out.println("Show " + logViewTable.refreshOnShow);
                if (!logViewTable.refreshOnShow) {
                    return;
                }
                logViewTable.readFile(logViewTable.logFile);
                //log.trace("Reread file for logViewTable");
            }
        });
    // Config Tab		
        JPanel configPane = new JPanel();
        tabbedPane.addTab("Configuraton", null, configPane, null);
        configPane.setLayout(null);

        // Open file panel
        JPanel panelOpenFile = new JPanel();
        panelOpenFile.setBounds(5, 5, 554, 54);
        panelOpenFile.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "Log File",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        configPane.add(panelOpenFile);
        SpringLayout sl_panelOpenFile = new SpringLayout();
        panelOpenFile.setLayout(sl_panelOpenFile);
        // Open file name text field in open file panel 
        txtFileName = new JTextField();
        sl_panelOpenFile.putConstraint(SpringLayout.NORTH, txtFileName,
                5, SpringLayout.NORTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.SOUTH, txtFileName,
                -5, SpringLayout.SOUTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.EAST, txtFileName,
                -55, SpringLayout.EAST, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.WEST, txtFileName, 5,
                SpringLayout.WEST, panelOpenFile);
        panelOpenFile.add(txtFileName);
        // Select file button in open file panel 
        JButton btnOpenFile = new JButton("...");
        sl_panelOpenFile.putConstraint(SpringLayout.NORTH, btnOpenFile, 5,
                SpringLayout.NORTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.SOUTH, btnOpenFile, -5,
                SpringLayout.SOUTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.EAST, btnOpenFile, -5,
                SpringLayout.EAST, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.WEST, btnOpenFile, 5,
                SpringLayout.EAST, txtFileName);
        // Click event for select file button - open file dialog 
        btnOpenFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Log File", "log");
                fileChooser.setFileFilter(filter);
                fileChooser.setCurrentDirectory(new File(folder));
                int result = fileChooser.showDialog(null, "Open Log File");
                if (result == JFileChooser.APPROVE_OPTION) {
                    fileLog = fileChooser.getSelectedFile();
                    folder = fileLog.getParent();
                    String logFileName = fileLog.getAbsolutePath();
                    logger.fine("Using file " + logFileName);
                    txtFileName.setText(logFileName);
                    logViewTable.readFile(logFileName);

                    timer.cancel();
                    timerTask = new DirWatcher(window);
                    timer = new Timer();
                    timer.schedule(timerTask, 2000, 1000);

                    //String zipFileName = "f:\\eclipse\\data\\2015-12-10\\2015-12-10_172642.zip";
                    //System.out.println(folder + "\\" + logViewTable.files.getLast().getName());
                    readZipFile(folder + "\\" + logViewTable.files.getLast().getName());
                }
            }
        });
        panelOpenFile.add(btnOpenFile);

        JCheckBox chckbxRemoveDuplicateShots = new JCheckBox("Remove Duplicate Shots");
        chckbxRemoveDuplicateShots.setBounds(15, 66, 195, 23);
        configPane.add(chckbxRemoveDuplicateShots);

        chckbxAdjustForToday = new JCheckBox("Adjust Data Folder for Today");
        chckbxAdjustForToday.setBounds(210, 66, 195, 23);
        configPane.add(chckbxAdjustForToday);

        chckbxShowMarkers = new JCheckBox("Show Markers");
        chckbxShowMarkers.setBounds(15, 96, 195, 23);
        configPane.add(chckbxShowMarkers);

        chckbxShowPreviousShot = new JCheckBox("Show Previous Plot");
        chckbxShowPreviousShot.setBounds(210, 96, 195, 23);
        configPane.add(chckbxShowPreviousShot);

        chckbxShowHyst = new JCheckBox("Show Hystogramm");
        chckbxShowHyst.setBounds(15, 126, 195, 23);
        configPane.add(chckbxShowHyst);

        /*		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setBounds(5, 269, 100, 23);
		configPane.add(comboBox);
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"One", "Two", "Three"}));
         */
        JScrollPane scrollPane_2 = new JScrollPane();
        JLabel lbl_2 = new JLabel("Incuded columns");
        lbl_2.setHorizontalAlignment(SwingConstants.CENTER);
        //lbl_2.setFont(new Font("Tahoma", Font.PLAIN, 16));
        scrollPane_2.setColumnHeaderView(lbl_2);
        scrollPane_2.setBounds(5, 155, 160, 160);
        configPane.add(scrollPane_2);

        txtarIncludedColumns = new JTextArea();
        txtarIncludedColumns.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent ev) {
                //log.trace("Change Update");
            }

            @Override
            public void insertUpdate(DocumentEvent ev) {
                //log.trace("Insert Update");
                logViewTable.setIncludedSignalNames(txtarIncludedColumns.getText());
                logViewTable.refreshOnShow = true;
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                //log.trace("Remove Update");
                logViewTable.setIncludedSignalNames(txtarIncludedColumns.getText());
                logViewTable.refreshOnShow = true;
            }
        });
        txtarIncludedColumns.setText("Time\nShot\nU_ex");
        scrollPane_2.setViewportView(txtarIncludedColumns);

        JScrollPane scrollPane_3 = new JScrollPane();
        JLabel lbl_3 = new JLabel("Excuded columns");
        lbl_3.setHorizontalAlignment(SwingConstants.CENTER);
        scrollPane_3.setColumnHeaderView(lbl_3);
        scrollPane_3.setBounds(180, 155, 160, 160);
        configPane.add(scrollPane_3);

        txtarExcludedColumns = new JTextArea();
        txtarExcludedColumns.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent ev) {
                //log.trace("Change Update");
            }

            @Override
            public void insertUpdate(DocumentEvent ev) {
                //log.trace("Insert Update");
                logViewTable.setExcludedSignalNames(txtarExcludedColumns.getText());
                logViewTable.refreshOnShow = true;
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                //log.trace("Remove Update");
                logViewTable.setExcludedSignalNames(txtarExcludedColumns.getText());
                logViewTable.refreshOnShow = true;
            }
        });
        txtarExcludedColumns.setText("File\nRF_PHASE\nS_C1(A)");
        scrollPane_3.setViewportView(txtarExcludedColumns);
    }

    private void restoreConfig() {
        String logFileName = null;
        try {
            ObjectInputStream objIStrm = new ObjectInputStream(new FileInputStream("config.dat"));

            Rectangle bounds = (Rectangle) objIStrm.readObject();
            frame.setBounds(bounds);

            logFileName = (String) objIStrm.readObject();
            txtFileName.setText(logFileName);
            fileLog = new File(logFileName);

            String str = (String) objIStrm.readObject();
            folder = str;

            str = (String) objIStrm.readObject();
            txtarExcludedColumns.setText(str);

            str = (String) objIStrm.readObject();
            txtarIncludedColumns.setText(str);

            boolean sm = (boolean) objIStrm.readObject();
            chckbxShowMarkers.setSelected(sm);

            boolean sp = (boolean) objIStrm.readObject();
            chckbxShowPreviousShot.setSelected(sp);

            objIStrm.close();

            logger.fine("Config restored.");
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.WARNING, "Config read error {0}", e);
        }
        timer.cancel();
        timer = new Timer();
        timerTask = new DirWatcher(window);
        timer.schedule(timerTask, 2000, 1000);

        // Add event listener for logview table
        ListSelectionModel rowSM = logViewTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                //Ignore extra messages.
                if (event.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) event.getSource();
                if (lsm.isSelectionEmpty()) {
                    //System.out.println("No rows selected.");
                } else {
                    int selectedRow = lsm.getMaxSelectionIndex();
                    //System.out.println("Row " + selectedRow + " is now selected.");
                    //String fileName = folder + "\\" + logViewTable.files.get(selectedRow);
                    try {
                        File zipFile = logViewTable.files.get(selectedRow);
                        readZipFile(zipFile);
                        if (timerTask != null && timerTask.timerCount > 0) {
                            dimLineColor();
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Selection change exception ", e);
                        //panel.removeAll();
                    }
                }
            }
        });

        logViewTable.readFile(logFileName);
    }

    private void saveConfig() {
        timer.cancel();

        Rectangle bounds = frame.getBounds();
        String txt = txtFileName.getText();
        txt = fileLog.getAbsolutePath();
        String txt1 = txtarExcludedColumns.getText();
        String txt2 = txtarIncludedColumns.getText();
        boolean sm = chckbxShowMarkers.isSelected();
        boolean sp = chckbxShowPreviousShot.isSelected();
        try {
            ObjectOutputStream objOStrm = new ObjectOutputStream(new FileOutputStream("config.dat"));
            objOStrm.writeObject(bounds);
            objOStrm.writeObject(txt);
            objOStrm.writeObject(folder);
            objOStrm.writeObject(txt1);
            objOStrm.writeObject(txt2);
            objOStrm.writeObject(sm);
            objOStrm.writeObject(sp);
            objOStrm.close();
            logger.fine("Config saved.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Config write error ", e);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        saveConfig();
        System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        restoreConfig();
    }

    void readZipFile(File zipFile) {
        if (zipFile == null) {
            return;
        }
        if (!zipFile.exists()) {
            return;
        }
        readZipFile(zipFile.getAbsolutePath());
    }

    void readZipFile(String fileName) {
        if (fileName == null || "".equals(fileName)) {
            return;
        }

        List<String> signalList = SignalPlotter.readSignalList(fileName);

        int n = panel.getComponentCount();
        Component[] components = panel.getComponents();
        //log.trace(n);

        panel.removeAll();
        //List<String> columnList = logViewTable.get();
        for (String str : signalList) {
            SignalChartPanel chart = new SignalChartPanel();
            chart.readParameters(fileName, str);
            if (logViewTable.findColumn(chart.getLabel()) >= 0) {
                chart.readData(fileName, str);
                chart.setChartParam();
                chart.setLineColor(2, freshColor);
                chart.setPreferredSize(new Dimension(320, 250));

                XYSeries prevSignal = null;
                if (chckbxShowPreviousShot.isSelected()) {
                    for (Component component : components) {
                        if (component instanceof SignalChartPanel) {
                            SignalChartPanel c = (SignalChartPanel) component;
                            if (c.getLabel().equals(chart.getLabel())) {
                                prevSignal = c.getSeries(2);
                                c.removeAllSeries();
                                c = null;
                                break;
                            }
                        }
                    }
                    if (prevSignal != null) {
                        //log.trace(prevSignal.getKey());
                        prevSignal.setKey("Previous");
                        chart.addSeries(prevSignal);
                        chart.setLineColor(3, Color.yellow);
                    }
                }

                if (!chckbxShowMarkers.isSelected()) {
                    chart.clearPlotMarkers();
                }

                if (chckbxShowHyst.isSelected()) {
                    prevSignal = chart.getSeries(2);
                    SignalChartPanel chartHyst = new SignalChartPanel();
                    n = prevSignal.getItemCount();

                    double min = prevSignal.getMinY();
                    double max = prevSignal.getMaxY();

                    int m = 250;
                    double[] hyst = new double[m + 1];
                    double delta = (prevSignal.getMaxY() - prevSignal.getMinY()) / m;

                    double sum = 0.0;
                    double count = 0.0;
                    int j = 0;
                    for (int i = 0; i < n; i++) {
                        j = (int) (((double) prevSignal.getY(i) - prevSignal.getMinY()) / delta);
                        hyst[j]++;
                        sum = sum + (double) prevSignal.getY(i);
                        count++;
                    }
                    double avg = sum / count;

                    XYSeries series = new XYSeries("Hyst");
                    for (int i = 0; i < m; i++) {
                        series.add(i * delta + prevSignal.getMinY(), hyst[i]);
                    }

                    chartHyst.addSeries(series);
                    panel.add(chartHyst);
                }

                panel.add(chart);
            }
        }
        panel.updateUI();
        lblZipFileName.setText("File : " + fileName);
    }

    List<String> readDirZip(String folder) {
        File dir = new File(folder);
        return readDirZip(dir);
    }

    List<String> readDirZip(File dir) {
        if (dir.isFile()) {
            dir = dir.getParentFile();
        }

        LinkedList<String> result = new LinkedList<String>();
        if (!dir.isDirectory()) {
            return result;
        }

        String[] dirList = dir.list();
        if (dirList == null) {
            return result;
        }
        String folder = dir.getName() + File.pathSeparator;
        for (String str : dirList) {
            if (str.endsWith(".zip")) {
                result.add(folder + str);
            }
        }
        return result;
    }

    public static void setLineColor(JPanel panel, Color color) {
        Component[] plots = panel.getComponents();
        for (Component p : plots) {
            if (!(p instanceof SignalChartPanel)) {
                continue;
            }
            ((SignalChartPanel) p).setLineColor(2, color);
        }
    }

    public void dimLineColor() {
        setLineColor(panel, dimmedColor);
    }

    public String getLogFileName() {
        return fileLog.getAbsolutePath();
        //return txtFileName.getText();
    }

    public String getRootFolderName() {
        File logFile = new File(getLogFileName());
        return logFile.getParentFile().getParentFile().getParentFile().getParent();
    }

    public File getFileLog() {
        return fileLog;
    }

    public void setFileLog(File file) {
        fileLog = file;
    }

    public void setFileLog(String fileName) {
        fileLog = new File(fileName);
    }

    public File getFileZip() {
        return fileZip;
    }

    public File getFileFolder() {
        return fileLog.getParentFile();
    }

    public File getRootFolder() {
        return fileLog.getParentFile().getParentFile().getParentFile().getParentFile();
    }

    public boolean checkLock() {
        File lockFile = new File(folder, "lock.lock");
        return lockFile.exists(); //System.out.println("Locked");
        //System.out.println("Unlocked");
    }

}

class DirWatcher extends TimerTask {

    //private static final Logger logger = LogManager.getLogger(DirWatcher.class);
    private static final Logger logger = Logger.getLogger(DirWatcher.class.getName());

    LoggerPlotter loggerPlotter = null;

    int timerCount = 0;
    int timerCountMax = 15;

    String folder = "";
    int oldnFiles = 0;
    int nFiles = 0;
    int count = 0;
    //String[] oldDirList = new String[0];
    //String[] dirList = new String[0];
    long oldLogFileLength = 0;

    LinkedList<String> dirList;
    LinkedList<String> oldDirList;

    public DirWatcher(LoggerPlotter lp) {
        ///super();
        timerCount = 0;
        timerCountMax = 15;

        this.loggerPlotter = lp;
        this.folder = lp.folder;
        File dir = new File(folder);
        //oldDirList = dir.list();
        oldDirList = new LinkedList<>(Arrays.asList(dir.list()));
        //oldnFiles = oldDirList.length;
        oldnFiles = oldDirList.size();
        count = 0;
        oldLogFileLength = 0;
    }

    @Override
    public void run() {
        timerCount++;
        //log.trace("Timer ", timerCount);
        if (timerCount == timerCountMax) {
            loggerPlotter.dimLineColor();
            //timerCount = 0;
            //log.trace("Line color dimmed.");
        }
        // Current log file name
        String logFileName = loggerPlotter.getLogFileName();
        //log.trace("Log file name " + logFileName);
        if (logFileName == null) {
            return;
        }

        // Determine today log file name
        String rootFolder = loggerPlotter.getRootFolderName();
        String todayFolder = LoggerDumper.getLogFolderName();
        String todayFile = LoggerDumper.getLogFileName();
        String todayLogFileName = rootFolder + "\\" + todayFolder + "\\" + todayFile;
        //log.trace("Today log file name " + todayLogFileName);

        if (loggerPlotter.chckbxAdjustForToday.isSelected() && !logFileName.equals(todayLogFileName)) {
            File newLogFile = new File(todayLogFileName);
            if (newLogFile.exists()) {
                logger.log(Level.INFO, "Today log file found. Changing to {0}", todayLogFileName);
                logFileName = todayLogFileName;
                loggerPlotter.txtFileName.setText(logFileName);
                loggerPlotter.setFileLog(logFileName);
                loggerPlotter.folder = newLogFile.getParent();
                oldDirList = new LinkedList<>();
                oldLogFileLength = 0;
                loggerPlotter.logViewTable.readFile(logFileName);
            }
        }
        if (loggerPlotter.checkLock()) {
            return;
        }

        File logFile = new File(logFileName);
        long logFileLength = logFile.length();
        if (logFileLength <= oldLogFileLength) {
            return;
        }
        logger.info("Logfile length has increaed.");
        try {
            FileWriter fw = new FileWriter(logFile, true);
            fw.close();
        } catch (IOException e) {
            logger.info("Log file is not writable.");
            return;
        }

        File dir = new File(loggerPlotter.folder);
        String[] dirListArray = dir.list();
        dirList = new LinkedList<String>(Arrays.asList(dirListArray));
        //nFiles = dirList.length;
        nFiles = dirList.size();

        String addedFileName = null;
        int addedFiles = 0;
        for (String str : dirList) {
            //if (!StrArr.contains(oldDirList, str)) { 
            if (!oldDirList.contains(str)) {
                //log.trace("Added file " + str);
                if (str.endsWith(".zip")) {
                    addedFileName = str;
                    addedFiles++;
                }
            }
        }

        String deletedFileName = null;
        int deletedFiles = 0;
        for (String str : oldDirList) {
            //if (!StrArr.contains(dirList, str)) { 
            if (!dirList.contains(str)) {
                //log.trace("Deleted file " + str);
                deletedFileName = str;
                deletedFiles++;
            }
        }

        if (addedFileName != null) {
            logger.info("New files found. Replot.");
            timerCount = 0;
            loggerPlotter.logViewTable.readFile(logFileName);
        }

        oldnFiles = nFiles;
        oldDirList = dirList;
        oldLogFileLength = logFileLength;
    }
}
