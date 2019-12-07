import java.awt.BorderLayout;
import javax.swing.*;

public class Driver
{
	/**
	 * Creates the frame of the GUI and a DatabasePanel
	 */
	public static void main(String[] args)
	{
		//TODO CREATE FRAME CLASS??
		JFrame frame = new JFrame("MLB Database");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new LoginPanel(frame));
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null); //center on screen
//		frame.setLayout(new BorderLayout()); //TODO IS THIS NECESSARY?
//		frame.getContentPane().add(new DatabasePanel(), BorderLayout.CENTER);
//		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		frame.setVisible(true);
	}
}
