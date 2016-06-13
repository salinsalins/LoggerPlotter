package binp.nbi.tango.adc;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileOpenPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 8871748358055212398L;
    private JTextField txtFileName = null;
    private JButton btnOpenFile = null;
    private File file;

    public FileOpenPanel(String title, final FileNameExtensionFilter filter) {
        super();
        setBorder(new TitledBorder(new EtchedBorder(
                        EtchedBorder.LOWERED, null, null), title,
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));

        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        // Open file name text field in open file panel 
        txtFileName = new JTextField();
        layout.putConstraint(SpringLayout.NORTH, txtFileName,
                        5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.SOUTH, txtFileName,
                        -5, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, txtFileName,
                        -55, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.WEST, txtFileName, 5,
                        SpringLayout.WEST, this);
        add(txtFileName);

        // Select file button in open file panel 
        btnOpenFile = new JButton("...");
        layout.putConstraint(SpringLayout.NORTH, btnOpenFile, 5,
                        SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.SOUTH, btnOpenFile, -5,
                        SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, btnOpenFile, -5,
                        SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.WEST, btnOpenFile, 5,
                        SpringLayout.EAST, txtFileName);
        add(btnOpenFile);
        // Click event for select file button - open file dialog 
        btnOpenFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                //FileNameExtensionFilter filter;
                //filter = new FileNameExtensionFilter("Log file", "log");
                fileChooser.setFileFilter(filter);
                fileChooser.setCurrentDirectory(file.getParentFile());
                int result = fileChooser.showDialog(null, "Open File");
                if (result == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    txtFileName.setText(file.getPath());
                }
            }
        });          
    }

    public FileOpenPanel() {
        this("Select Log File", new FileNameExtensionFilter("Log file", "log"));
    }

    public void setText(String txt){
        txtFileName.setText(txt);
    }

    public String getText() {
        return txtFileName.getText();
    }

    public File getFile() {
        return file;
    }
    
    public String getFileName() {
        return file.getName();
    }
    
    public void setFile(File newFile) {
        file = newFile;
    }

    public void setFile(String fileName) {
        file = new File(fileName);
    }

    public File getFolder() {
        return file.getParentFile();
    }

    public String getFolderName() {
        return file.getParent();
    }
}
