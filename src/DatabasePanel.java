import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

//TODO REMOVE SAVED QUERY
//TODO keep query history, add buttons to save, paste to execute box, remove. execute directly? copy to clipboard?
//TODO CONCURRENCY CONTROL?
//TODO LOG IN AS READONLY USER???

public class DatabasePanel extends JPanel
{
	private static final String SAVED_QUERIES_FILENAME = "savedqueries.txt";
	
	private Connection conn = null;
	private Statement stmt;
	private ResultSet rSet;
	private ResultSetMetaData rsmd;
	//private JButton updateTables;
	private JComboBox<String> tableSelect;
	private JTextArea queryText;
	private JButton executeQuery;
	private JScrollPane tableScroll;
	private QueryHistory<String> queryHistory;
	private JButton saveQuery;
	private JList<String> savedQueries;
	private DefaultListModel<String> savedQueriesModel; //For the JList of saved queries
	private JTable table;
	private File savedQueriesFile;
	private boolean changesMade = false; //determines if any changes have been made since the last table update TODO CONCURRENCY CONTROL
	
	//---------------------------------------------------------------------
	// Constructor. Sets up the panel by creating the components and 
	// adding them to the panel.
	//---------------------------------------------------------------------
	public DatabasePanel()
	{
		try
		{
			connectToDatabase();
			
			//updateTables = new JButton("Update tables");
			
			table = new JTable();
			//table.setPreferredSize(new Dimension(2000, 1000));
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setEnabled(false);
			
			tableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			//tableScroll.setPreferredSize(new Dimension(1800, 800));
			//tableScroll.setViewportView(table);
			
			//TODO SEPARATE PANELS FOR THE JLISTS AND STUFF?
			
			//set up the query history list
			queryHistory = new QueryHistory<String>();
			JScrollPane historyScroll = new JScrollPane();
			historyScroll.setPreferredSize(new Dimension(200, 300));
			historyScroll.setViewportView(queryHistory);
			
			//Set up the list of saved queries
			savedQueriesModel = new DefaultListModel<String>();
			savedQueries = new JList<String>(savedQueriesModel);
			JScrollPane savedQueriesScroll = new JScrollPane();
			savedQueriesScroll.setPreferredSize(new Dimension(200, 300));
			savedQueriesScroll.setViewportView(savedQueries);
			
			loadSavedQueries();
			
			//TODO HAVE USER SELECT TABLE TO BEGIN, OR INPUT QUERY? OR VIEW TABLES?
			refreshTable(table, "SELECT * FROM people;");
			
			queryText = new JTextArea();
			queryText.setLineWrap(true);
			JScrollPane queryScroll = new JScrollPane(queryText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			queryScroll.setPreferredSize(new Dimension(300, 50));
			
			executeQuery = new JButton("Execute Query");
			executeQuery.setEnabled(true); //TODO SET TO FALSE AND SET TO TRUE WHEN STUFF IS ENTERED
			
			saveQuery = new JButton("Save Query");
			saveQuery.setEnabled(true); //TODO SET TO FALSE AND SET TO TRUE WHEN STUFF IS ENTERED
			
			createTableComboBox();
			
			//Add the action listener
			EventListener listener = new EventListener();
			tableSelect.addActionListener(listener);
			executeQuery.addActionListener(listener);
			saveQuery.addActionListener(listener);
			queryHistory.addListSelectionListener(listener);
			savedQueries.addListSelectionListener(listener);
			
			//TODO FIGURE HOW TO NOT STRETCH ACROSS THE PANEL
			JPanel optionsPanel = new JPanel();
			//optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
			//optionsPanel.add(Box.createHorizontalStrut(200));
			optionsPanel.add(queryScroll);
			optionsPanel.add(executeQuery);
			optionsPanel.add(saveQuery);
			optionsPanel.add(new JLabel("History"));
			optionsPanel.add(historyScroll);
			optionsPanel.add(new JLabel("Saved queries"));
			optionsPanel.add(savedQueriesScroll);
			optionsPanel.add(new JLabel("Select a table:"));
			optionsPanel.add(tableSelect);
			
			//JPanel tablePanel = new JPanel();
			//tablePanel.add(new JLabel("Select a table:"));
			//tablePanel.add(tableSelect);
			//tablePanel.add(tableScroll);
			
			setLayout(new BorderLayout());
			add(optionsPanel, BorderLayout.NORTH);
			add(tableScroll, BorderLayout.CENTER);
//			add(updateTables);
			
			//setPreferredSize(new Dimension(tableScroll.getPreferredSize().width + 100, tableScroll.getPreferredSize().height + 100));
		} 
		catch (SQLException ex)// | IOException ex) 
		{
			//TODO DEBUG
			System.out.println(ex.getClass());
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			
			showErrorMessage("An error was encountered while trying to create the database.");
			System.exit(1);
		}
		catch (FileNotFoundException ex)
		{
			//TODO DEBUG
			System.out.println(ex.getClass());
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			
			saveQuery.setEnabled(false);
			showErrorMessage("Could not load saved queries:\n" + SAVED_QUERIES_FILENAME + " Could not be found.");
		}
	}

	/**
	 * Connects the program to the SQL database and creates the tables to 
	 * hold data on teams and games.
	 * @throws IOException 
	 */
	private void connectToDatabase() throws SQLException//, IOException
	{
		//TODO REFACTOR TEMP CONNECTION?
//		conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "password");
//		boolean createdDatabase = false;
//		
//		if (conn != null)
//		{
//			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//			ResultSet rset = stmt.executeQuery("SELECT * FROM pg_database WHERE datname = 'stats';");
//			if (!rset.next()) //if db does not exist, create it.
//			{
//				//TODO CREATE MAIN MENU TO DISPLAY DIALOG BOX WHEN DB IS BEING CREATED?	
//				System.out.println("creating db..."); //TODO REMOVE PRINT STATEMENTS
//				stmt.executeUpdate("CREATE DATABASE mlbtest;");
//				createdDatabase = true;
//			}
//			
//			stmt.close();
//			conn.close();
			
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stats?user=postgres&password=password&useSSL=false");
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//			
//			if (createdDatabase)
//			{
//				createTables();
//				System.out.println("created tables");
//			}
//		}
	}
	
	/**
	 * Executes the statements found in the sql file to create the tables.
	 * 
	 * @throws SQLException if there was an error executing the statements
	 * @throws IOException 
	 *//*
	private void createTables() throws SQLException, IOException
	{
		ArrayList<String> buildStatements = readFile("core" + File.separator + "_createtables.sql");
		for (String statement : buildStatements)
		{
			stmt.execute(statement);
		}
		
		CopyManager cm = new CopyManager((BaseConnection) conn);
		long rowsInserted = cm.copyIn(
	                "COPY people(player_id, birth_year, birth_month, birth_day, birth_country, birth_state, birth_city, death_year, death_month, death_day, death_country, death_state, death_city, name_first, name_last, name_given, weight, height, bats, throws, debut, final_game, retro_id, bbref_id) FROM STDIN (FORMAT csv, HEADER)", 
	                new BufferedReader(new FileReader("C:\\Users\\Danny\\workspace\\DBMSProject\\core\\People.csv"))
	                );
		//COPY people(player_id, birth_year, birth_month, birth_day, birth_country, birth_state, birth_city, death_year, death_month, death_day, death_country, death_state, death_city, name_first, name_last, name_given, weight, height, bats, throws, debut, final_game, retro_id, bbref_id) FROM 'C:\Users\Danny\workspace\DBMSProject\core\People.csv' DELIMITER ',' CSV HEADER;
		System.out.printf("%d row(s) inserted%n", rowsInserted);
	}*/
	
	/**
	 * Creates the combobox where tables can be selected.
	 * 
	 * @throws SQLException
	 */
	private void createTableComboBox() throws SQLException
	{
		tableSelect = new JComboBox<String>();
		
		DatabaseMetaData dmd = conn.getMetaData();
		ResultSet tableNames = dmd.getTables(conn.getCatalog(), null, null, new String[] {"TABLE"});
		while (tableNames.next())
		{
			tableSelect.addItem(tableNames.getString("TABLE_NAME"));
		}
		
		tableSelect.setSelectedItem("people");
	}
	
	/**
	 * Reads an sql file and returns a list of statements.
	 * 
	 * @param filepath the path to the file to read
	 * @return a list of statements to execute
	 * @throws FileNotFoundException if the file could not be found
	 *//*
	public ArrayList<String> readFile(String filepath) throws FileNotFoundException
	{
		File sqlFile = new File(filepath);
		Scanner scan = new Scanner(sqlFile, "Latin1");
		scan.useDelimiter(";");
		
		//create a list of statements to execute
		ArrayList<String> statements = new ArrayList<String>();
		while (scan.hasNext())
		{
			statements.add(scan.next());
		}
		
		scan.close();
		
		return statements;
	}*/
	
	/**
	 * Updates the JTables of the panel with data from the database.
	 * 
	 * @param table the JTable to update
	 * @param query the query to execute
	 */ //TODO CHANGE NAME?
	public void refreshTable(JTable table, String query) throws SQLException
	{
		rSet = stmt.executeQuery(query);
		rsmd = rSet.getMetaData();
		int columns = rsmd.getColumnCount();
		String[] columnInfo = new String[columns];
		
		for (int i = 1; i <= columns; i++)
		{
			columnInfo[i - 1] = rsmd.getColumnName(i);
		}
		
		DefaultTableModel model = new DefaultTableModel(columnInfo, 0);
		
		//will only execute if the ResultSet contains at least 1 entry
		while(rSet.next())
		{
			for (int i = 1; i <= columns; i++)
			{
				columnInfo[i - 1] = rSet.getString(i);
			}
			model.addRow(columnInfo);
		}
		
		table.setModel(model);
		changesMade = false;
		queryHistory.add(query);
	}
	
	/**
	 * Displays a dialog box with an error message to the user then exit the program.
	 * 
	 * @param message the message to show.
	 */
	private void showErrorMessage(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
		//System.exit(1);
	}
	
	/**
	 * Loads the saved queries from a text file upon starting the application.
	 * 
	 * @throws FileNotFoundException if savedqueries.txt could not be found
	 */
	private void loadSavedQueries() throws FileNotFoundException
	{
		savedQueriesFile = new File(SAVED_QUERIES_FILENAME);
		Scanner scan = new Scanner(savedQueriesFile);
		while (scan.hasNext())
		{
			savedQueriesModel.addElement(scan.nextLine());
		}
		
		scan.close();
	}
	
	/**
	 * Saves the query currently in the text box.
	 */
	private void saveQuery()
	{
		String savedQuery = queryText.getText();
		
		if (savedQuery.length() > 0)
		{
			//Find the correct sorted index for the new query to save
			int index = 0;
			while (index < savedQueriesModel.getSize() && savedQueriesModel.get(index).compareTo(savedQuery) < 0)
			{
				index++;
			}
			
			//If index == size, query will be inserted at the end. Also don't insert duplicate queries.
			if (index == savedQueriesModel.getSize() || !savedQueriesModel.get(index).equals(savedQuery))
			{
				savedQueriesModel.add(index, savedQuery);
				
				//Write the queries to the file
				try
				{
					PrintWriter printer = new PrintWriter(savedQueriesFile);
					
					for (int i = 0; i < savedQueriesModel.getSize(); i++)
					{
						printer.println(savedQueriesModel.get(i));
					}
					
					printer.close();
				}
				catch (IOException ex)
				{
					//TODO DEBUG
					ex.printStackTrace();
					
					showErrorMessage("The query could not be saved.");
					saveQuery.setEnabled(false);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(this, "This query has already been saved.", "Save query", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else
		{
			showErrorMessage("Query must not be empty.");
		}
	}
	
	private class EventListener implements ActionListener, ListSelectionListener
	{
		@Override
		public void actionPerformed(ActionEvent event) //ActionListener
		{
			if (event.getSource() == executeQuery) //button
			{
				if (queryText.getText() != "")
				{
					try
					{
						refreshTable(table, queryText.getText());
					}
					catch (SQLException ex)
					{
						showErrorMessage("Invalid query.\n" + ex.getMessage());
					}
				}
			}
			else if (event.getSource() == tableSelect) //combobox
			{
				try
				{
					refreshTable(table, "SELECT * FROM " + (String)tableSelect.getSelectedItem() + ";");
				}
				catch (SQLException ex)
				{
					//TODO DEBUG
					ex.printStackTrace();
					
					showErrorMessage("There was an error reading the table.\n" + ex.getMessage());
					System.exit(1);
				}
			}
			else if (event.getSource() == saveQuery)
			{
				saveQuery();
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent event) //ListSelectionListener
		{
			if (event.getSource() == queryHistory && !event.getValueIsAdjusting())
			{
				queryText.setText(queryHistory.getSelectedValue());
			}
			else if (event.getSource() == savedQueries && !event.getValueIsAdjusting())
			{
				//TODO???
			}
		}
	}
}
