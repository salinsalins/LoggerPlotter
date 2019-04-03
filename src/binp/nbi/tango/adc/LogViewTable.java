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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

public class LogViewTable extends JTable {

    private static final long serialVersionUID = 8656104666552673873L;
    private static final Logger LOGGER = Logger.getLogger(LogViewTable.class.getPackage().getName());

    private String[] includedSignalNames = {"Time", "Shot", "U_ex", "I_ex",
        "U_tot", "I_ac"};
    private String[] excludedSignalNames = {"File", "RF_PHASE", "S_C1(A)"};
    boolean excludeDuplicateShots = false;
//    boolean refreshOnShow = false;

    private List<String> included;
    private List<String> excluded;

    File logFile;
    LinkedList<File> files = new LinkedList<>();
    LinkedList<String> shots = new LinkedList<>();
    LinkedList<String> columns = new LinkedList<>();

    public LogViewTable() {
        super();
        LOGGER.setLevel(Level.FINE);

        this.included = new LinkedList<>();
        this.included.addAll(Arrays.asList("Time", "Shot", "U_ex", "I_ex", "U_tot", "I_ac"));
        this.excluded = new LinkedList<>();
        this.excluded.addAll(Arrays.asList("File", "RF_PHASE", "S_C1(A)"));

        //setPreferredScrollableViewportSize(new Dimension(500, 70));
        //setFillsViewportHeight(true);
        // font for cells
        setFont(new Font("SansSerif", Font.PLAIN, 16));
        setRowHeight(25);
        // font for headers
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

    public void setIncluded(String str) {
        String[] strarr = str.split("\n");
        included = new ArrayList<>();
        included.addAll(Arrays.asList(strarr));
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
        return excludeDuplicateShots;
    }

    /**
     * @param excludeDuplicatedShots the excludeDuplicatedShots to set
     */
    public void setExcludeDuplicatedShots(boolean excludeDuplicatedShots) {
        this.excludeDuplicateShots = excludeDuplicatedShots;
    }

    public boolean addColumn(String columnName) {
        return addColumn(columnName, null);
    }

    public boolean addColumn(String columnName, String cellValue) {
        if (columnName == null) {
            return false;
        }
        columnName = columnName.trim();
        if (columnName.isEmpty()) {
            return false;
        }
        DefaultTableModel model = (DefaultTableModel) getModel();
        int cc = model.getColumnCount();
        int rc = model.getRowCount();
        int col = model.findColumn(columnName);
        if (col < 0) {
            model.addColumn(columnName);
            columns.add(columnName);
            col = cc;
        }
        if (cellValue == null) {
            return true;
        }
        if (rc <= 0) {
            model.addRow(new String[cc]);
            rc = 1;
        }
        setValueAt(cellValue.trim(), rc-1, col);
        return true;
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
//        if (!isShowing()) {
            // log.trace("Table is Hidden");
//            refreshOnShow = true;
//            return;
//        }

//        refreshOnShow = false;
        files.clear();
        shots.clear();
        columns.clear();

        String[] zipsInDir = logFile.getParentFile().list(new FilenameFilter() { 
            public boolean accept(File dir, String filename) { 
            	return filename.endsWith(".zip");            }                 });

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
                // Split line to fields separated by Constants.LOG_DELIMETER ; by default
                fields = line.split(Constants.LOG_DELIMETER);
                // First field is the Date/Time of Shot in YYYY-MM-DD HH:mm:SS format
                if (fields.length > 1) {
                    // Add row to the model
                    model.addRow(new String[0]);
                    files.add(null);
                    shots.add(null);

                    // Split HH:mm:SS from Date/Time and add space in front for
                    // better look
                    nv = fields[0].split(" ");
                    if (nv.length < 2) 
                    	continue;
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

                        if ("Shot".equals(nv[0])) {
                            if (shots.contains(nv[1]) && excludeDuplicateShots) {
                                continue;
                            }
                            shots.set(shots.size()-1, nv[1]);
                        }

                        if ("File".equals(nv[0])) {
                            if (StringArray.contains(zipsInDir, nv[1])) {
                            	File zf = new File(logFile.getParentFile(), nv[1]);
                                files.set(files.size()-1, zf);
                            }
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
            LOGGER.log(Level.INFO, "File {0} not found", logFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "IOException ", e);
        }
    }

    public void scrollTo(int row, int col) {
       if (!(getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport) getParent();
        Rectangle rect = getCellRect(row, col, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
        repaint();
    }

    public void scrollTo(int row) {
        scrollTo(row, 0);
    }
    
    public void scrollTo() {
        int row = getRowCount() - 1;
        if (row >= 0) {
            scrollTo(row, 0);
        }
    }

    public void scrollToSelection() {
        int row = getSelectedRow();
        if (row >= 0) {
            scrollTo(row, 0);
        }
    }
    
    public void scrollToLastRow() {
        int lastRow = getRowCount() - 1;
        if (lastRow >= 0) {
            scrollTo(lastRow, 0);
            //setRowSelectionInterval(lastRow, lastRow);
        }
    }

    public void scrollToValue(String str) {
        int[] rc = find(str);
        if (rc[0] >= 0 && rc[1] >= 0) {
            scrollTo(rc[0], rc[1]);
        }
    }

    public int[] find(String str) {
        int[] result = {-1, -1};
        int rows = getRowCount();
        int cols = getColumnCount();
        if (rows <= 0 || cols <= 0) {
            return result;
        }
        TableModel model = getModel();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (str.equals(model.getValueAt(i, j).toString())) {
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
        LinkedList<int[]> result = new LinkedList<>();
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
        int column = findColumn(col);
        if (column < 0) {
            return -1;
        }
        return findInColumn(column, str);
    }

    public int findInColumn(int col, String str) {
        if (col < 0) {
            return -1;
        }
        int rows = getRowCount();
        if (rows <= 0) {
            return -1;
        }
        TableModel model = getModel();
        for (int i = 0; i < rows; i++) {
            if (model.getValueAt(i, col).equals(str)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getValueAt(int i, int j) {
        Object o = super.getValueAt(i, j);
        if(o != null) return o.toString();
        return "";
   }

    public List<String> getColumnNames() {
        List<String> list = new LinkedList<>();
        for (int i=0; i < getColumnCount(); i++) {
            list.add(getColumnName(i));
        }
        return list;
   }

    public void setColumnNames(List<String> columnNames) {
        List<String> oldColumnNames = getColumnNames();
        TableColumnModel tcm = getColumnModel();
        TableColumnModel ntcm = new DefaultTableColumnModel();
        for (int i=0; i < columnNames.size(); i++) {
            String column = columnNames.get(i);
            int index = oldColumnNames.indexOf(column);
            if(index >= 0) {
                ntcm.addColumn(tcm.getColumn(index));
                //System.out.printf("Switch %s %d and %d\n", column, index, i);
            } else {
                //addColumn(column);
            }
        }
        setColumnModel(ntcm);
   }

    public void saveAsCSV() {
        int nc = getColumnCount();
        int nr = getRowCount();
        int[] cw = new int[nc];
        String[] units = new String[nc];
        String str;
        String[] strarr;
        
        for (int j=0; j < nc; j++){
            str = getColumnName(j);
            if(cw[j] < str.length()) cw[j] = str.length(); 
        }            
        for (int i=0; i < nr; i++){
            for (int j=0; j < nc; j++){
                str = getValueAt(i, j);
                strarr = str.split(" ");
                if(strarr.length > 1) units[j] = strarr[1];
                else units[j] = "";
                if(cw[j] < strarr[0].length()) cw[j] = strarr[0].length(); 
            }            
        }
        for (int j=0; j < nc; j++){
            str = getColumnName(j);
            if(cw[j] < str.length()+units[j].length()+1) 
                cw[j] = str.length()+units[j].length()+1; 
        }            

        for (int j=0; j < nc-1; j++){
            System.out.printf("%"+cw[j]+"s; ", getColumnName(j)+" "+units[j]);
        }            
        System.out.printf("%"+cw[nc-1]+"s", getColumnName(nc-1)+" "+units[nc-1]);
        System.out.println();

        for (int i=0; i < nr; i++){
            for (int j=0; j < nc-1; j++){
                str = getValueAt(i, j);
                strarr = str.split(" ");
                System.out.printf("%"+cw[j]+"s; ", strarr[0]);
            }            
            str = getValueAt(i, nc-1);
            strarr = str.split(" ");
            System.out.printf("%"+cw[nc-1]+"s", strarr[0]);
            System.out.println();
        }
    }
}
