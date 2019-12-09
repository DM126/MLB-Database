import javax.swing.*;

public class Driver
{
	/**
	 * Creates the frame of the GUI and a login screen.
	 */
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("MLB Database");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new LoginPanel(frame));
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null); //center on screen
	}
}
