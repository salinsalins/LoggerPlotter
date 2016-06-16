package binp.nbi.tango.adc;

import binp.nbi.tango.util.Constants;
import binp.nbi.tango.util.StringArray;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LogViewTable extends JTable {

    private static final long serialVersionUID = 8656104666552673873L;
    //private static final Logger logger = LogManager.getLogger(LogViewTable.class.getName());
    private static final Logger logger = Logger.getLogger(LogViewTable.class.getName());

    private String[] includedSignalNames = {"Time", "Shot", "U_ex", "I_ex",
        "U_tot", "I_ac"};
    private String[] excludedSignalNames = {"File", "RF_PHASE", "S_C1(A)"};
    boolean excludeDuplicatedShots = false;
    boolean refreshOnShow = false;

    File logFile;
    LinkedList<File> files;
    LinkedList<String> shots;
    LinkedList<String> columns;

    public LogViewTable() {
        super();
        setPreferredScrollableViewportSize(new Dimension(500, 70));
        setFillsViewportHeight(true);
        setFont(new Font("SansSerif", Font.PLAIN, 16));
        setRowHeight(25);
        getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public LogViewTable(String fileName) {
        this();
        readFile(fileName);
    }

    public LogViewTable(File file) {
        this();
        readFile(file);
    }

    /**
     * @return the includedSignalNames
     */
    public String[] getIncludedSignalNames() {
        return includedSignalNames;
    }

    /**
     * @param includedSignalNames the includedSignalNames to set
     */
    public void setIncludedSignalNames(String[] includedSignalNames) {
        this.includedSignalNames = includedSignalNames;
    }

    public void setIncludedSignalNames(String includedSignalNames) {
        String[] stringArray = includedSignalNames.split("\n");
        // System.out.println("Included " + stringArray.length);
        setIncludedSignalNames(stringArray);
    }

    /**
     * @return the excludedSignalNames
     */
    public String[] getExcludedSignalNames() {
        return excludedSignalNames;
    }

    /**
     * @param excludedSignalNames the excludedSignalNames to set
     */
    public void setExcludedSignalNames(String[] excludedSignalNames) {
        this.excludedSignalNames = excludedSignalNames;
    }

    public void setExcludedSignalNames(String excludedSignalNames) {
        String[] stringArray = excludedSignalNames.split("\n");
        // System.out.println("Excluded " + stringArray.length);
        setExcludedSignalNames(stringArray);
    }

    /**
     * @return the excludeDuplicatedShots
     */
    public boolean isExcludeDuplicatedShots() {
        return excludeDuplicatedShots;
    }

    /**
     * @param excludeDuplicatedShots the excludeDuplicatedShots to set
     */
    public void setExcludeDuplicatedShots(boolean excludeDuplicatedShots) {
        this.excludeDuplicatedShots = excludeDuplicatedShots;
    }

    public boolean addColumn(String columnName) {
        columnName = columnName.trim();
        if (columnName == null || "".equals(columnName)) {
            return false;
        }
        DefaultTableModel model = (DefaultTableModel) getModel();
        int i = model.findColumn(columnName);
        if (i < 0) {
            model.addColumn(columnName);
            // System.out.println("Added column " + columnName);
            columns.add(columnName);
            return true;
        }
        // System.out.println("Column exists " + columnName + " " + i);
        return false;
    }

    public void addColumn(String columnName, String cellValue) {
        columnName = columnName.trim();
        if (columnName == null || "".equals(columnName)) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) getModel();
        int row = model.getRowCount() - 1;
        if (row < 0) {
            model.addRow(new String[0]);
            row = model.getRowCount() - 1;
        }
        int col = model.findColumn(columnName);
        if (col < 0) {
            model.addColumn(columnName);
            columns.add(columnName);
            col = model.findColumn(columnName);
        }
        setValueAt(cellValue.trim(), row, col);
    }

    public final void readFile(String fileName) {
        readFile(new File(fileName));
    }

    public final void readFile(File file) {
        if (!file.canRead()) {
            return;
        }
        logFile = file;
        readFile();
    }

    public void readFile() {
        if (!isShowing()) {
            // log.trace("Table is Hidden");
            refreshOnShow = true;
            return;
        }

        refreshOnShow = false;
        files = new LinkedList<>();
        shots = new LinkedList<>();
        columns = new LinkedList<>();

        // Set new empty model
        DefaultTableModel model = new DefaultTableModel();
        setModel(model);

        // Add first column "Time"
        addColumn("Time");

        // Add columns from includedSignalNames
        for (String str : includedSignalNames) {
            addColumn(str);
        }

        try {
            // Open log file
            FileReader fr = new FileReader(logFile);
            BufferedReader bReader = new BufferedReader(fr);

            String line;
            String[] fields;
            String[] nv;

            // Read log file line by line
            while ((line = bReader.readLine()) != null) {
                // System.out.println(line);

                // Split line to fields separated by Constants.LOG_DELIMETER
                // ("; " by default)
                fields = line.split(Constants.LOG_DELIMETER);

                // First field is the Date/Time of Shot in YYYY-MM-DD HH:mm:SS
                // format
                if (fields.length > 1) {
                    // Add row to the model
                    model.addRow(new String[0]);
                    files.add(null);
                    shots.add(null);

                    // Split HH:mm:SS from Date/Time and add space in front for
                    // better look
                    nv = fields[0].split(" ");
                    if (nv.length < 2) continue;
                    addColumn("Time", " " + nv[1].trim());

                    // Iterate for other fields in the line
                    for (int i = 1; i < fields.length; i++) {
                        // Split each field for as "name = value" pair in the
                        // nv[2]
                        if (fields[i].contains(Constants.PROP_VAL_DELIMETER)) {
                            nv = fields[i].split(Constants.PROP_VAL_DELIMETER);
                        } else if (fields[i].contains(Constants.PROP_VAL_DELIMETER_OLD)) {
                            nv = fields[i].split(Constants.PROP_VAL_DELIMETER_OLD);
                        } else {
                            continue;
                        }
                        if (2 != nv.length) continue;
                        
                        nv[0] = nv[0].trim();
                        nv[1] = nv[1].trim();
                        // System.out.println(nv[0] + " -- " + nv[1]);

                        if (nv[0].equals("File")) {
                            // log.trace(marks[i]);
                            String zipFileName = new File(nv[1]).getName();
                            File zipFile = new File(logFile.getParentFile(), zipFileName);
                            if (zipFile.exists()) {
                                // log.trace("Add zip file to list " +
                                // zipFileName);
                                files.set(files.size()-1, zipFile);
                            }
                        }

                        if (nv[0].equals("Shot")) {
                            if (shots.contains(nv[1]) && excludeDuplicatedShots) {
                                continue;
                            }
                            shots.set(shots.size()-1, nv[1]);
                        }

                        if (StringArray.contains(excludedSignalNames, nv[0])) {
                            continue;
                        }

                        addColumn(nv[0], nv[1]);
                    }
                }
            }
            bReader.close();
            scrollToLastRow();
        } catch (FileNotFoundException e) {
            logger.log(Level.INFO, "File {0} not found", logFile.getAbsolutePath());
        } catch (IOException e) {
            logger.log(Level.INFO, "IOException ", e);
        }
    }

    public void scrollTo(int rowIndex, int colIndex) {
        // System.out.printf("Scroll To : %d %d\n", rowIndex, colIndex);
        if (!(getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport) getParent();
        Rectangle rect = getCellRect(rowIndex, colIndex, true);
        // System.out.println("rect = " + rect);
        Point pt = viewport.getViewPosition();
        // System.out.println("pt = " + pt);
        // int vh = viewport.getHeight();
        // System.out.println("vh = " + vh);
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        // System.out.println("rect = " + rect);
        // viewport.scrollRectToVisible(new Rectangle(0, 0, rect.width,
        // rect.height));
        viewport.scrollRectToVisible(rect);
        repaint();
    }

    public void scrollToLastRow() {
        int lastRow = getRowCount() - 1;
        if (lastRow >= 0) {
            scrollTo(lastRow, 0);
            setRowSelectionInterval(lastRow, lastRow);
        }
    }

    public void scrollToValue(String str) {
        int[] rc = find(str);
        if (rc[0] >= 0) {
            scrollTo(rc[0], rc[1]);
        }
    }

    public int[] find(String str) {
        int[] result = {-1, -1};
        int rows = getRowCount();
        int cols = getColumnCount();
        TableModel model = getModel();
        if (rows <= 0 || cols <= 0) {
            return result;
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (model.getValueAt(i, j).equals(str)) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return result;
    }

    public int[] findLast(String str) {
        int[] result = {-1, -1};
        int rows = getRowCount();
        int cols = getColumnCount();
        if (rows <= 0 || cols <= 0) {
            return result;
        }
        TableModel model = getModel();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (model.getValueAt(i, j).equals(str)) {
                    result[0] = i;
                    result[1] = j;
                }
            }
        }
        return result;
    }

    public List<int[]> findAll(String str) {
        LinkedList<int[]> result = new LinkedList<int[]>();
        int rows = getRowCount();
        int cols = getColumnCount();
        if (rows <= 0 || cols <= 0) {
            return result;
        }
        TableModel model = getModel();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (model.getValueAt(i, j).equals(str)) {
                    int[] t = {i, j};
                    result.add(t);
                }
            }
        }
        return result;
    }

    public int findColumn(String str) {
        return ((AbstractTableModel) getModel()).findColumn(str);
    }

    public int findInColumn(String col, String str) {
        int colN = findColumn(col);
        if (colN < 0) {
            return -1;
        }
        return findInColumn(colN, str);
    }

    public int findInColumn(int coli, String str) {
        if (coli < 0) {
            return -1;
        }
        int rows = getRowCount();
        if (rows <= 0) {
            return -1;
        }
        TableModel model = getModel();
        for (int i = 0; i < rows; i++) {
            if (model.getValueAt(i, coli).equals(str)) {
                return i;
            }
        }
        return -1;
    }

    public static void scrollTableTo(JTable table, int rowIndex, int colIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(rowIndex, colIndex, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
    }
}
