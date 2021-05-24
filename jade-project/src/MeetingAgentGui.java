package jadeproject;
import jade.core.AID;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

class MeetingAgentGui extends JFrame{
    
    private MeetingAgent myAgent; //The agent that will manage this Gui
    
    MeetingAgentGui(MeetingAgent a) {
		super(a.getLocalName());
		
		myAgent = a; 
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2)); //Create the panel that will have a button to schedule a meeting
		JButton b1 = new JButton("Made a Meeting");// The button I was talkin about
		
		b1.addActionListener(new ActionListener () {//This Listener will call the function of the agent to schedule a meeting
			public void actionPerformed(ActionEvent ev) {
				try { myAgent.programMeeting(); //Here 
				} catch (Exception e) {
					JOptionPane.showMessageDialog(MeetingAgentGui.this, e.getMessage());
				}//We put a try catch to manage exceptions
			}
		});
	    
		
		b1.setPreferredSize(new Dimension(300,30));// We put the size of the button
		panel.add(b1);//We add to the panel made before
		
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		setResizable(false);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();//The function if the user clicks the close window button.
			}
		});
    }
    
    public void display(){//The function to display the gUI in the location that we want, the center.
        pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centX = (int) screenSize.getWidth() / 2;
		int centY = (int) screenSize.getHeight() / 2;
		setLocation(centX - getWidth() / 2, centY - getHeight() / 2);
		setVisible(true);
        
    }
    
}