package unitCalc;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class CalcGUI extends JFrame {
	
	JPanel mainPanel;
	JTextArea txta;
	JTextField inputField;
	
	LinkedList<String> previousInputs = new LinkedList<String>();
	int previousInputIndex = -1;
	
	
	class Calculate implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String input = inputField.getText();
			Calculator.inform(input);
			previousInputs.addFirst(input);
			previousInputIndex = -1;
			Calculator.calculate(input);
			inputField.setText("");
		}
	}
	
	
	public CalcGUI() {
		
		Calculator.initCalculator();
		Function.initFunctionMap();
		setTitle("Unit calculator "+Calculator.version);
		
		JPanel middlePanel = new JPanel ();

	    JTextArea display = new JTextArea ( 30, 58 );
	    display.setEditable ( false ); // set textArea non-editable
	    JScrollPane scroll = new JScrollPane ( display );
	    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
	    Calculator.setResultArea(display);

	    //Add Textarea in to middle panel
	    middlePanel.add ( scroll );

	    
	    inputField = new JTextField(30);
	    middlePanel.add(inputField);
	    
	    
	    JButton calculateButton = new JButton("Calculate");
	    calculateButton.addActionListener(new Calculate());
	    calculateButton.setToolTipText("Click to perform the calcultation");
	    middlePanel.add(calculateButton);
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
		
	    add(middlePanel);
	    pack();
	    setLocationRelativeTo (null);
	    setVisible (true);
		
	}

	public static void main(String[] args) {
		new CalcGUI();
	}
}
