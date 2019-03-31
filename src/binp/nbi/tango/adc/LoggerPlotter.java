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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.data.xy.XYSeries;

public class LoggerPlotter extends WindowAdapter {
	// configure logging
	static {
    	System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] %4$-7s %2$s %5$s %6$s %n");
    }
    private static final Logger LOGGER = Logger.getLogger(LoggerPlotter.class.getPackage().getName());
    private static final ConsoleHandler handler = new ConsoleHandler();
    static {
    	LOGGER.addHandler(handler);
    	LOGGER.setUseParentHandlers(false);
    	LOGGER.setLevel(Level.FINE);
    	handler.setLevel(Level.FINE);
    }

    public static final String version = "4.0";

    File logFile = null;
    File zipFile = null;
    File rootFolder = new File(".\\");

    String folder = ".\\";

    DirWatcher timerTask = null;
    Timer timer = new Timer();

    static LoggerPlotter window;
    JFrame frame;
    JTabbedPane tabbedPane;

    
    JTextField jtfFileName;
    LogViewTable logViewTable;
    JPanel jpMainPanel;
    JLabel lblZipFileName;

    JTextArea jtaIncCol;
    JTextArea jtaExcCol;

    Color colFresh = new Color(255, 255, 255);
    Color colDimmed = new Color(0, 255, 0);
    JCheckBox jcbAdjustForToday;
    JCheckBox jcbShowMarkers;
    JCheckBox jcbShowPreviousShot;
    JCheckBox jcbShowHyst;
    JCheckBox jcbRemoveDuplicateShots;
    
    NewJPanel njp_1;
    
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
                    //for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    //    if ("Nimbus".equals(info.getName())) {
                    //        UIManager.setLookAndFeel(info.getClassName());
                    //        break;
                    //    }
                    //}
                    window = new LoggerPlotter();
                    window.frame.setVisible(true);
                    LOGGER.log(Level.FINE, "LoggerPlotter " + version + " started.");
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "LoggerPlotter run exceptoin");
                    LOGGER.log(Level.INFO, "Exception info", ex);
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
        frame.setBounds(100, 100, 600, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(this);
        frame.getContentPane().setLayout(new CardLayout(0, 0));

        // main tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        frame.getContentPane().add(tabbedPane, "name_5266296004328937");

        // 1st tab Signals and Log		
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.9);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        tabbedPane.addTab("Signals and Log", null, splitPane, null);

        // logview table in scroll pane for bottom part
        JScrollPane scrollPane_0 = new JScrollPane();
        splitPane.setRightComponent(scrollPane_0);
        logViewTable = new LogViewTable();
        scrollPane_0.setViewportView(logViewTable);
        // Add event listener for logview table
        logViewTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                //System.out.println(arg0.getButton());
                if(arg0.getButton() != MouseEvent.BUTTON3) return;
                logViewTable.saveAsCSV();
            }
        });
        ListSelectionModel lsm = logViewTable.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                //Ignore extra messages.
                if (event.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) event.getSource();
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMaxSelectionIndex();
                    try {
                        readZipFile(logViewTable.files.get(selectedRow));
                        if (timerTask != null && timerTask.timerCount > 0) {
                            dimLineColor();
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Selection change exception ");
                        LOGGER.log(Level.INFO, "Exception: ", ex);
                    }
                }
            }
        });

        // signal plots in scroll pane for top part
        JScrollPane scrollPane_1 = new JScrollPane();
        splitPane.setLeftComponent(scrollPane_1);
        jpMainPanel = new JPanel();
        jpMainPanel.setLayout(new GridLayout(0, 3, 5, 5));
        scrollPane_1.setViewportView(jpMainPanel);

        lblZipFileName = new JLabel("File :");
        lblZipFileName.setHorizontalAlignment(SwingConstants.CENTER);
        lblZipFileName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        scrollPane_1.setColumnHeaderView(lblZipFileName);

//        splitPane.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentShown(ComponentEvent arg0) {
//                System.out.println("Show " + logViewTable.refreshOnShow);
//                if (!logViewTable.refreshOnShow) {
//                    return;
//                }
//                logViewTable.readFile(logViewTable.logFile);
//                //log.trace("Reread file for logViewTable");
//            }
//        });
    
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
        jtfFileName = new JTextField();
        sl_panelOpenFile.putConstraint(SpringLayout.NORTH, jtfFileName,
                5, SpringLayout.NORTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.SOUTH, jtfFileName,
                -5, SpringLayout.SOUTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.EAST, jtfFileName,
                -55, SpringLayout.EAST, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.WEST, jtfFileName, 5,
                SpringLayout.WEST, panelOpenFile);
        panelOpenFile.add(jtfFileName);
        // Select file button in open file panel 
        JButton btnOpenFile = new JButton("...");
        sl_panelOpenFile.putConstraint(SpringLayout.NORTH, btnOpenFile, 5,
                SpringLayout.NORTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.SOUTH, btnOpenFile, -5,
                SpringLayout.SOUTH, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.EAST, btnOpenFile, -5,
                SpringLayout.EAST, panelOpenFile);
        sl_panelOpenFile.putConstraint(SpringLayout.WEST, btnOpenFile, 5,
                SpringLayout.EAST, jtfFileName);
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
                    logFile = fileChooser.getSelectedFile();
                    folder = logFile.getParent();
                    String logFileName = logFile.getAbsolutePath();
                    LOGGER.fine("Using file " + logFileName);
                    jtfFileName.setText(logFileName);
                    logViewTable.readFile(logFileName);

                    timer.cancel();
                    timerTask = new DirWatcher(window);
                    timer = new Timer();
                    timer.schedule(timerTask, 2000, 1000);

                    readZipFile(folder + "\\" + logViewTable.files.getLast());
                }
            }
        });
        panelOpenFile.add(btnOpenFile);

        jcbRemoveDuplicateShots = new JCheckBox("Remove Duplicate Shots");
        jcbRemoveDuplicateShots.setBounds(15, 66, 195, 23);
        configPane.add(jcbRemoveDuplicateShots);

        jcbAdjustForToday = new JCheckBox("Adjust Data Folder for Today");
        jcbAdjustForToday.setBounds(210, 66, 195, 23);
        configPane.add(jcbAdjustForToday);

        jcbShowMarkers = new JCheckBox("Show Markers");
        jcbShowMarkers.setBounds(15, 96, 195, 23);
        configPane.add(jcbShowMarkers);

        jcbShowPreviousShot = new JCheckBox("Show Previous Plot");
        jcbShowPreviousShot.setBounds(210, 96, 195, 23);
        configPane.add(jcbShowPreviousShot);

        jcbShowHyst = new JCheckBox("Show  Hystogramm");
        jcbShowHyst.setBounds(15, 126, 195, 23);
        configPane.add(jcbShowHyst);

        JScrollPane scrollPane_2 = new JScrollPane();
        JLabel lbl_2 = new JLabel("Included columns");
        lbl_2.setHorizontalAlignment(SwingConstants.CENTER);
        //lbl_2.setFont(new Font("Tahoma", Font.PLAIN, 16));
        scrollPane_2.setColumnHeaderView(lbl_2);
        scrollPane_2.setBounds(5, 155, 160, 160);
        configPane.add(scrollPane_2);
        jtaIncCol = new JTextArea();
        jtaIncCol.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent ev) {
                //log.trace("Change Update");
            }

            @Override
            public void insertUpdate(DocumentEvent ev) {
                //log.trace("Insert Update");
                logViewTable.setIncludedSignalNames(jtaIncCol.getText());
                logViewTable.refreshOnShow = true;
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                //log.trace("Remove Update");
                logViewTable.setIncludedSignalNames(jtaIncCol.getText());
                logViewTable.refreshOnShow = true;
            }
        });
        jtaIncCol.setText("Time\nShot\nU_ex");
        scrollPane_2.setViewportView(jtaIncCol);

        JScrollPane scrollPane_3 = new JScrollPane();
        JLabel lbl_3 = new JLabel("Excluded columns");
        lbl_3.setHorizontalAlignment(SwingConstants.CENTER);
        scrollPane_3.setColumnHeaderView(lbl_3);
        scrollPane_3.setBounds(180, 155, 160, 160);
        configPane.add(scrollPane_3);
        jtaExcCol = new JTextArea();
        jtaExcCol.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent ev) {
                //log.trace("Change Update");
            }

            @Override
            public void insertUpdate(DocumentEvent ev) {
                //log.trace("Insert Update");
                logViewTable.setExcludedSignalNames(jtaExcCol.getText());
                logViewTable.refreshOnShow = true;
            }

            @Override
            public void removeUpdate(DocumentEvent ev) {
                //log.trace("Remove Update");
                logViewTable.setExcludedSignalNames(jtaExcCol.getText());
                logViewTable.refreshOnShow = true;
            }
        });
        jtaExcCol.setText("File\nRF_PHASE\nS_C1(A)");
        scrollPane_3.setViewportView(jtaExcCol);

        njp_1 = new NewJPanel();
        njp_1.setBorder(new TitledBorder(new EtchedBorder(
                EtchedBorder.LOWERED, null, null), "New Pane",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        njp_1.setBounds(5, 355, 360, 260);
        configPane.add(njp_1);
    }
    
    private void restartTimerTask() {
        timer.cancel();
        timer = new Timer();
        timerTask = new DirWatcher(window);
        timer.schedule(timerTask, 2000, 1000);
    }

    public void restoreConfig() {
        Rectangle bnds = new Rectangle(20, 20, 200, 200);
        String lfn = "";
        String fld = "";
        String exc = "";
        String inc = "";
        boolean sm = false;
        boolean sp = false;
        List<String> cln = new ArrayList<String>();
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("config.dat"));

            bnds = (Rectangle) ois.readObject();
            frame.setBounds(bnds);

            lfn = (String) ois.readObject();

            fld = (String) ois.readObject();
            folder = fld;

            exc = (String) ois.readObject();
            jtaExcCol.setText(exc);

            inc = (String) ois.readObject();
            jtaIncCol.setText(inc);

            sm = (boolean) ois.readObject();
            jcbShowMarkers.setSelected(sm);

            sp = (boolean) ois.readObject();
            jcbShowPreviousShot.setSelected(sp);
            
            cln = (List<String>) ois.readObject();

            ois.close();

            logFile = new File(lfn);
            if(logFile.exists()) {
            	jtfFileName.setText(lfn);
    	        logViewTable.readFile(lfn);
    	        logViewTable.setColumnNames(cln);
    	        logViewTable.clearSelection();
    	        logViewTable.changeSelection(logViewTable.getRowCount()-1, 0, false, false);
    	        logViewTable.scrollToLastRow();
            }
            else {
            	logFile = null;
            	jtfFileName.setText("");
            }
	        
	        restartTimerTask();

	        LOGGER.fine("Config restored");
        } catch (Exception ex) {
        	// restore default config
            timer.cancel();
            jtfFileName.setText("");
            logFile = null;
            zipFile = null;
            LOGGER.log(Level.WARNING, "Config read error");
            LOGGER.log(Level.INFO, "Excption: ", ex);
        }
   }

    public void saveConfig() {
        timer.cancel();
        
        Rectangle bounds = frame.getBounds();
        String txt = jtfFileName.getText();
        txt = logFile.getAbsolutePath();
        String txt1 = jtaExcCol.getText();
        String txt2 = jtaIncCol.getText();
        boolean sm = jcbShowMarkers.isSelected();
        boolean sp = jcbShowPreviousShot.isSelected();
        List<String> columnNames = logViewTable.getColumnNames();
        List<Object> state = njp_1.getState();
        try {
            ObjectOutputStream objOStrm = new ObjectOutputStream(new FileOutputStream("config.dat"));
            objOStrm.writeObject(bounds);
            objOStrm.writeObject(txt);
            objOStrm.writeObject(folder);
            objOStrm.writeObject(txt1);
            objOStrm.writeObject(txt2);
            objOStrm.writeObject(sm);
            objOStrm.writeObject(sp);
            objOStrm.writeObject(columnNames);
            objOStrm.writeObject(state);
            objOStrm.close();
            LOGGER.fine("Config saved.");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Config write error");
            LOGGER.log(Level.INFO, "Exception info", ex);
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
        List<String> cols = logViewTable.getColumnNames();
        List<String> labels = new ArrayList<String>();
        List<String> ssig = new ArrayList<String>();

        SignalChartPanel cp = new SignalChartPanel();
        for (String s : signalList) {
            cp.readParameters(fileName, s);
            labels.add(cp.getLabel());
        }

        for (String c : cols) {
            if (labels.contains(c)) 
            	ssig.add(signalList.get(labels.indexOf(c)));
        }

        int n = jpMainPanel.getComponentCount();
        Component[] components = jpMainPanel.getComponents();

    	jpMainPanel.removeAll();
        for (String s : ssig) {
            SignalChartPanel chart = new SignalChartPanel();
            chart.readParameters(fileName, s);
            if (logViewTable.findColumn(chart.getLabel()) >= 0) {
                chart.readData(fileName, s);
                chart.setChartParam();
                chart.setLineColor(2, colFresh);
                chart.setPreferredSize(new Dimension(320, 250));

                XYSeries prevSignal = null;
                if (jcbShowPreviousShot.isSelected()) {
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

                if (!jcbShowMarkers.isSelected()) {
                    chart.clearPlotMarkers();
                }

                if (jcbShowHyst.isSelected()) {
                    prevSignal = chart.getSeries(2);
                    SignalChartPanel chartHyst = new SignalChartPanel();
                    n = prevSignal.getItemCount();

                    double min = prevSignal.getMinY();
                    double max = prevSignal.getMaxY();

                    int m = 250;
                    double[] hyst = new double[m + 1];
                    double delta = (prevSignal.getMaxY() - min) / m;

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
                    jpMainPanel.add(chartHyst);
                }

                jpMainPanel.add(chart);
            }
        }
        //jpMainPanel.updateUI();
        lblZipFileName.setText("File : " + fileName);
    }

    List<String> listDirectory(File dir, String extension) {
        List<String> result = new LinkedList<>();

        if (dir.isFile()) {
            dir = dir.getParentFile();
        }
        if (!dir.isDirectory()) {
            return result;
        }
        String[] dirList = dir.list();
        if (dirList == null) {
            return result;
        }
        String folder = dir.getAbsolutePath() + File.pathSeparator;
        for (String str : dirList) {
            if (str.endsWith(extension)) {
                result.add(folder + str);
            }
        }
        return result;
    }

    public static void setLineColor(JPanel panel, Color color) {
        Component[] plots = panel.getComponents();
        for (Component p : plots) {
            if (p instanceof SignalChartPanel) {
                ((SignalChartPanel) p).setLineColor(2, color);
            }
        }
    }

    public void dimLineColor() {
        setLineColor(jpMainPanel, colDimmed);
    }

    public String getLogFileName() {
        return logFile.getAbsolutePath();
        //return txtFileName.getText();
    }

    public String getRootFolderName() {
        File logFile = new File(getLogFileName());
        return logFile.getParentFile().getParentFile().getParentFile().getParent();
    }

    public void setFileLog(String fileName) {
        logFile = new File(fileName);
    }

    public File getFileZip() {
        return zipFile;
    }

    public File getFileFolder() {
        return logFile.getParentFile();
    }

    public File getRootFolder() {
        return logFile.getParentFile().getParentFile().getParentFile().getParentFile();
    }

    public boolean checkLock() {
        File lockFile = new File(folder, "lock.lock");
        return lockFile.exists(); 
        //System.out.println("Locked");
        //System.out.println("Unlocked");
    }

    //**************************************************************************
    class DirWatcher extends TimerTask {
        LoggerPlotter loggerPlotter = null;
        int timerCount = 0;
        int timerCountMax = 15;
        String folder = "";
        int oldnFiles = 0;
        int nFiles = 0;
        int count = 0;
        long oldLogFileLength = 0;
        List<String> dirList;
        List<String> oldDirList;

        public DirWatcher(LoggerPlotter lp) {
            timerCount = 0;
            timerCountMax = 15;

            this.loggerPlotter = lp;
            this.folder = lp.folder;
            File dir = new File(folder);
            String[] dl = dir.list();
            if (dl != null) {
            	oldDirList = Arrays.asList(dir.list());
            }
            else {
            	LOGGER.log(Level.WARNING, "Directory {0} is not found", folder);
            	oldDirList = new ArrayList<String>();
            }
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

            if (loggerPlotter.jcbAdjustForToday.isSelected() && !logFileName.equals(todayLogFileName)) {
                File newLogFile = new File(todayLogFileName);
                if (newLogFile.exists()) {
                    LOGGER.log(Level.INFO, "Today log file found. Changing to {0}", todayLogFileName);
                    logFileName = todayLogFileName;
                    loggerPlotter.jtfFileName.setText(logFileName);
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
            if (!logFile.exists()) {
                LOGGER.info("Logfile does not exist");
            	return;
            }
            long logFileLength = logFile.length();
            if (logFileLength <= oldLogFileLength) {
                return;
            }
            LOGGER.info("Logfile length has increaed.");
            try {
                FileWriter fw = new FileWriter(logFile, true);
                fw.close();
            } catch (IOException e) {
                LOGGER.info("Log file is not writable.");
                return;
            }

            File dir = new File(loggerPlotter.folder);
            String[] dirListArray = dir.list();
            dirList = Arrays.asList(dirListArray);
            nFiles = dirList.size();

            String addedFileName = null;
            int addedFiles = 0;
            for (String str : dirList) {
                if (!oldDirList.contains(str)) {
                    if (str.endsWith(".zip")) {
                        addedFileName = str;
                        addedFiles++;
                    }
                }
            }

            String deletedFileName = null;
            int deletedFiles = 0;
            for (String str : oldDirList) {
                if (!dirList.contains(str)) {
                    deletedFileName = str;
                    deletedFiles++;
                }
            }

            if (addedFileName != null) {
                LOGGER.info("New files found. Replot.");
                timerCount = 0;
                loggerPlotter.logViewTable.readFile(logFileName);
            }

            oldnFiles = nFiles;
            oldDirList = dirList;
            oldLogFileLength = logFileLength;
        }
    }
}
