package binp.nbi.tango.adc;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class LogViewPanel extends JPanel {
    private static final long serialVersionUID = 8656104666552673873L;

    JTable table = new JTable();

    public LogViewPanel() {
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);
        SpringLayout layout = new SpringLayout();
        scrollPane.setLayout(layout);

        table = new JTable();
        layout.putConstraint(SpringLayout.NORTH, table,
                        5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.SOUTH, table,
                        -5, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, table,
                        -5, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.WEST, table, 5,
                        SpringLayout.WEST, this);
        scrollPane.add(table);

        TableModel model = new DefaultTableModel(3, 8); 
        table.setModel(model);
    }
}
