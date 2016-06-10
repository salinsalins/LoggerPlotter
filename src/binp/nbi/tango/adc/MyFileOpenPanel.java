package binp.nbi.tango.adc;

import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class MyFileOpenPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8251981739974731848L;
	JTextField txtFileName;
	JButton btnOpenFile;

	public MyFileOpenPanel(String name) {
		setBounds(0, 0, 200, 54);
		setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), name,
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		// Text field with file name
		JTextField txtFileName = new JTextField();
		layout.putConstraint(SpringLayout.NORTH, txtFileName,
				5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, txtFileName,
				-5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, txtFileName,
				-55, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, txtFileName, 5,
				SpringLayout.WEST, this);
		//txtFileName.set
		add(txtFileName);
		// Button to select file
		JButton btnOpenFile = new JButton("...");
		layout.putConstraint(SpringLayout.NORTH, btnOpenFile, 5,
				SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, btnOpenFile, -5,
				SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, btnOpenFile, -5,
				SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, btnOpenFile, 5,
				SpringLayout.EAST, txtFileName);
		add(btnOpenFile);
	}

	public MyFileOpenPanel() {
		this("Select File");
	}
	
	public void addMouseListener(MouseListener ml) {
		btnOpenFile.addMouseListener(ml);
	}
	
	public void setText(String txt){
		txtFileName.setText(txt);
	}

	public String getText() {
		return txtFileName.getText();
	}
}
