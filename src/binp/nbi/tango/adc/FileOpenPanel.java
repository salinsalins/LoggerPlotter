package binp.nbi.tango.adc;

import java.awt.LayoutManager;
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

	protected String folder;

	public FileOpenPanel() {
		// TODO Auto-generated constructor stub
		// Open file panel
		setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "Open File",
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

		// Click event for select file button - open file dialog 
		btnOpenFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Log File", "log");
				fileChooser.setFileFilter(filter);
				fileChooser.setCurrentDirectory(new File(folder));
				int result = fileChooser.showDialog(null, "Open File");
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					txtFileName.setText(file.getPath());
					folder = file.getParent();

					//logViewTable.readFile(file.getPath());

					//myTimer.cancel();
					//myTask = new DirWatcher(window);
					//myTimer = new Timer();
					//myTimer.schedule(myTask, 2000, 1000);

					//readZipFileList(folder + "\\" + logViewTable.files.getLast()); 
				}
			}
		});
		add(btnOpenFile);
	}

	public FileOpenPanel(LayoutManager arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public FileOpenPanel(boolean arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public FileOpenPanel(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
