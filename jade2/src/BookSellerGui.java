package jadelab2;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class BookSellerGui extends JFrame {	
	private BookSellerAgent myAgent;
	
	private JTextField titleField, priceField, shipmentField;
	
	BookSellerGui(BookSellerAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 4));
		p.add(new JLabel("Title:"));
		titleField = new JTextField(15);
		p.add(titleField);
		p.add(new JLabel("Price:"));
		priceField = new JTextField(15);
		p.add(priceField);
		//Exercise1
		p.add(new JLabel("Shipment Cost:"));
		shipmentField= new JTextField(15);
		p.add(shipmentField);
		//
		
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = titleField.getText().trim();
					String price = priceField.getText().trim();
					String shipment= shipmentField.getText().trim();
					myAgent.updateCatalogue(title, Integer.parseInt(price)+Integer.parseInt(shipment));
					titleField.setText("");
					priceField.setText("");
					shipmentField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(BookSellerGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void display() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}	
}
