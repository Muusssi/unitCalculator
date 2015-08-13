package unitCalc;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class CalcGUI extends JFrame {
	
	JPanel mainPanel;
	JTextArea txta;
	JTextField inputField;
	
	LinkedList<String> previousInputs = new LinkedList<String>();
	int previousInputIndex = -1;
	
	
	JRadioButtonMenuItem showErrorMenuItem;
	JRadioButtonMenuItem showErrorPercentageMenuItem;
	JRadioButtonMenuItem showErrorRangeMenuItem;
	
	
	class Calculate implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String input = inputField.getText();
			Calculator.inform(input);
			if (!input.equals("") && (previousInputs.size() == 0 || !input.equals(previousInputs.getFirst()))) {
				previousInputs.addFirst(input);
				previousInputIndex = -1;
			}
			Calculator.calculate(input);
			inputField.setText("");
		}
	}
	
	public void switchMeasurementError() {
		if (Calculator.useMeasurementError) {
			showErrorPercentageMenuItem.setEnabled(true);
			showErrorPercentageMenuItem.setVisible(true);
			showErrorRangeMenuItem.setEnabled(true);
			showErrorRangeMenuItem.setVisible(true);
			showErrorMenuItem.setSelected(true);
		}
		else {
			showErrorPercentageMenuItem.setEnabled(false);
			showErrorPercentageMenuItem.setVisible(false);
			showErrorRangeMenuItem.setEnabled(false);
			showErrorRangeMenuItem.setVisible(false);
			showErrorMenuItem.setSelected(false);
		}
	}
	
	
	public CalcGUI() {
		
		Calculator.initCalculator();
		Function.initFunctionMap();
		setTitle("Unit calculator "+Calculator.version);
		Calculator.GUI = this;
		
		JPanel middlePanel = new JPanel(new GridBagLayout());

	    JTextArea display = new JTextArea(30, 58);
	    display.setEditable(false); // set textArea non-editable
	    JScrollPane scroll = new JScrollPane(display);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    DefaultCaret caret = (DefaultCaret)display.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    Calculator.setResultArea(display);

	    //Add Textarea in to middle panel
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 0.5;
	    c.gridx = 0;
	    c.gridy = 0;
	    middlePanel.add(scroll, c);

	    
	    inputField = new JTextField(30);
	    c = new GridBagConstraints();
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 0.5;
	    c.gridx = 0;
	    c.gridy = 1;
	    middlePanel.add(inputField, c);
	    
	    
	    JButton calculateButton = new JButton("Calculate");
	    calculateButton.addActionListener(new Calculate());
	    calculateButton.setToolTipText("Click to perform the calcultation");
	    c = new GridBagConstraints();
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 0.5;
	    c.gridx = 1;
	    c.gridy = 1;
	    middlePanel.add(calculateButton, c);
	    getRootPane().setDefaultButton(calculateButton);
	    
	    
		
	    // Setting up exit listener
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		
		inputField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
					if (previousInputIndex+1 < previousInputs.size()) {
						previousInputIndex++;
						inputField.setText(previousInputs.get(previousInputIndex));
					}
				}
				else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					if (previousInputIndex > 0) {
						previousInputIndex--;
						inputField.setText(previousInputs.get(previousInputIndex));
					}
					else if (previousInputIndex == 0) {
						previousInputIndex = -1;
						inputField.setText("");
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				//Do nothing
			}

			@Override
			public void keyTyped(KeyEvent e) {
				// Do nothing
			}
			
		});
		
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		
		showErrorMenuItem = new JRadioButtonMenuItem("Use measurement error");
		showErrorMenuItem.setSelected(false);
		showErrorMenuItem.setMnemonic(KeyEvent.VK_R);
		optionsMenu.add(showErrorMenuItem);
		showErrorMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Calculator.useMeasurementError = ((JRadioButtonMenuItem)arg0.getSource()).isSelected();
				switchMeasurementError();
			}
		});
		
		
		showErrorPercentageMenuItem = new JRadioButtonMenuItem("Show measurement error percentage");
		showErrorPercentageMenuItem.setSelected(false);
		showErrorPercentageMenuItem.setEnabled(false);
		optionsMenu.add(showErrorPercentageMenuItem);
		showErrorPercentageMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Calculator.showErrorPercentage = ((JRadioButtonMenuItem)arg0.getSource()).isSelected();
			}
		});
		
		showErrorRangeMenuItem = new JRadioButtonMenuItem("Show measurement error range");
		showErrorRangeMenuItem.setSelected(false);
		showErrorRangeMenuItem.setEnabled(false);
		optionsMenu.add(showErrorRangeMenuItem);
		showErrorRangeMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Calculator.showErrorRange = ((JRadioButtonMenuItem)arg0.getSource()).isSelected();
			}
		});
		
		
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		
		JMenuItem menuItemManual = new JMenuItem("Manual (Finnish)", KeyEvent.VK_T);
		helpMenu.add(menuItemManual);
		menuItemManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
			        Desktop.getDesktop().browse(new URL("https://github.com/Muusssi/unitCalculator/wiki/Manual-(Finnish)").toURI());
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			}
		});
		
		/**/
		JMenuItem menuItemUnitList = new JMenuItem("List of units (Finnish)", KeyEvent.VK_T);
		helpMenu.add(menuItemUnitList);
		menuItemUnitList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
			        Desktop.getDesktop().browse(new URL("https://github.com/Muusssi/unitCalculator/wiki/Units-and-measures-(Finnish)").toURI());
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			}
		});
		
		
	    add(middlePanel);
	    pack();
	    setLocationRelativeTo (null);
	    setVisible (true);
		
	}

	public static void main(String[] args) {
		new CalcGUI();
	}
}
