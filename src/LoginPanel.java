import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;

public class LoginPanel extends JPanel
{
	private JFrame parent;
	private JTextField username;
	private JPasswordField password;
	private JButton login;
	
	public LoginPanel(JFrame parent)
	{
		this.parent = parent;
		
		username = new JTextField(20);
		password = new JPasswordField(20);
		login = new JButton("Log in");
		
		JPanel textFields = new JPanel();
		textFields.add(new JLabel("Username: "));
		textFields.add(username);
		textFields.add(new JLabel("Password: "));
		textFields.add(password);
		textFields.setPreferredSize(new Dimension(username.getPreferredSize().width + 100, username.getPreferredSize().height * 3));
		
		ButtonListener listener = new ButtonListener();
		login.addActionListener(listener);
		password.addActionListener(listener);
		
		JLabel title = new JLabel("MLB Stats Database");
		title.setFont(new Font("Arial", Font.BOLD, 20));
		
		JPanel stuffPanel = new JPanel();
		stuffPanel.add(new JLabel("Log in to postgres"));		
		stuffPanel.add(textFields);
		stuffPanel.add(login);
		
		stuffPanel.setPreferredSize(new Dimension(textFields.getPreferredSize().width, textFields.getPreferredSize().height * 2 + 10));
		
		add(title);
		add(stuffPanel);
		
		setPreferredSize(new Dimension(stuffPanel.getPreferredSize().width, stuffPanel.getPreferredSize().height + title.getPreferredSize().height + 20));
	}
	
	/**
	 * Switches to the database panel.
	 * 
	 * @param conn the connection to the stats database
	 */
	private void openDatabase(Connection conn)
	{
		parent.getContentPane().removeAll();
		parent.setLayout(new BorderLayout()); //TODO IS THIS NECESSARY?
		parent.getContentPane().add(new DatabasePanel(conn), BorderLayout.CENTER);
		parent.setExtendedState(JFrame.MAXIMIZED_BOTH);
		parent.setVisible(true);
	}
	
	//TODO DUPLICATE CODE
	/**
	 * Displays a dialog box with an error message to the user then exit the program.
	 * 
	 * @param message the message to show.
	 */
	private void showErrorMessage(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Attempts to connect to the database using the login information.
	 * 
	 * @param user the user to log in as
	 * @param password the password for the user
	 */
	private void connectToDatabase(String user, String password)
	{
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stats", user, password);
			openDatabase(conn);
		}
		catch (SQLException ex)
		{
			//TODO DEBUG
			System.out.println(ex.getClass());
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			
			showErrorMessage("Could not connect to the database.\n" + ex.getMessage());
		}
	}
	
	private class ButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			//click login button or press enter on password field
			if (event.getSource() == login || event.getSource() == password)
			{
				connectToDatabase(username.getText(), new String(password.getPassword()));
			}
		}
		
	}
}
