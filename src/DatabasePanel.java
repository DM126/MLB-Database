import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.sql.*;

//TODO LOG IN AS READONLY USER???

public class DatabasePanel extends JPanel
{	
	private Connection conn = null;
	private Statement stmt;
	private JComboBox<String> tableSelect; //Selects which table to display
	private JTextArea queryText; //Where the user will enter a query
	private JButton executeQuery;
	private QueryHistory<String> queryHistory; //Stores the history for the current session
	private JButton saveQuery;
	private JList<String> savedQueries;
	private DefaultListModel<String> savedQueriesModel; //For the JList of saved queries
	private JButton deleteQuery; //used for deleting saved queries
	private TableRowSorter<TableModel> tableSorter;
	private JTable table;
	private QuerySaver querySaver;

	/**
	 * Sets up the panel by creating the components and  adding them to the 
	 * panel.
	 */
	public DatabasePanel()
	{
		try
		{
			connectToDatabase();
			
			table = new JTable();
			//table.setPreferredSize(new Dimension(2000, 1000));
			//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			table.setEnabled(false);
			
			//For sorting the table by columns
			tableSorter = new TableRowSorter<TableModel>();
			table.setRowSorter(tableSorter);
			
			JScrollPane tableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
			savedQueries.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane savedQueriesScroll = new JScrollPane();
			savedQueriesScroll.setPreferredSize(new Dimension(200, 300));
			savedQueriesScroll.setViewportView(savedQueries);
			
			querySaver = new QuerySaver(savedQueriesModel);
			querySaver.loadSavedQueries();
			
			deleteQuery = new JButton("Delete query");
			deleteQuery.setEnabled(!savedQueriesModel.isEmpty()); //enable the delete button if saved queries exist
			
			//TODO HAVE USER SELECT TABLE TO BEGIN, OR INPUT QUERY? OR VIEW TABLES?
			refreshTable("SELECT * FROM people;");
			
			queryText = new JTextArea();
			queryText.setLineWrap(true);
			JScrollPane queryScroll = new JScrollPane(queryText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			queryScroll.setPreferredSize(new Dimension(300, 50));
			
			executeQuery = new JButton("Execute Query");
			executeQuery.setEnabled(true); //TODO SET TO FALSE AND SET TO TRUE WHEN STUFF IS ENTERED
			
			saveQuery = new JButton("Save Query");
			saveQuery.setEnabled(true); //TODO SET TO FALSE AND SET TO TRUE WHEN STUFF IS ENTERED
			
			createTableComboBox();
			
			//Add the event listeners
			EventListener listener = new EventListener();
			tableSelect.addActionListener(listener);
			executeQuery.addActionListener(listener);
			saveQuery.addActionListener(listener);
			deleteQuery.addActionListener(listener);
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
			optionsPanel.add(deleteQuery);
			optionsPanel.add(new JLabel("Select a table:"));
			optionsPanel.add(tableSelect);
			
			//JPanel tablePanel = new JPanel();
			//tablePanel.add(new JLabel("Select a table:"));
			//tablePanel.add(tableSelect);
			//tablePanel.add(tableScroll);
			
			setLayout(new BorderLayout());
			add(optionsPanel, BorderLayout.NORTH);
			add(tableScroll, BorderLayout.CENTER);
			
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
			showErrorMessage("Could not load saved queries:\n" + QuerySaver.SAVED_QUERIES_FILENAME + " Could not be found.");
		}
	}

	/**
	 * Connects the program to the database.
	 * 
	 * @throws SQLException if the database could not be connected to 
	 */
	private void connectToDatabase() throws SQLException//, IOException
	{		
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stats?user=postgres&password=password&useSSL=false");
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
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
	private void refreshTable(String query) throws SQLException
	{
		ResultSet rSet = stmt.executeQuery(query);
		ResultSetMetaData rsmd = rSet.getMetaData();
		int columns = rsmd.getColumnCount();
		String[] columnInfo = new String[columns];
		
		//Get the header info
		for (int i = 1; i <= columns; i++)
		{
			columnInfo[i - 1] = rsmd.getColumnName(i);
		}
		
		DefaultTableModel model = new DefaultTableModel(columnInfo, 0);
		
		//Fill out the row information
		while(rSet.next())
		{
			for (int i = 1; i <= columns; i++)
			{
				columnInfo[i - 1] = rSet.getString(i);
			}
			model.addRow(columnInfo);
		}
		
		table.setModel(model);
		tableSorter.setModel(model);
		
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
						refreshTable(queryText.getText());
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
					refreshTable("SELECT * FROM " + (String)tableSelect.getSelectedItem() + ";");
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
				if (queryText.getText() != "")
				{
					try
					{
						if (querySaver.saveQuery(queryText.getText()))
						{
							deleteQuery.setEnabled(true);
						}
						else
						{
							showErrorMessage("This query has already been saved.");
						}
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
					showErrorMessage("Query must not be empty.");
				}
			}
			else if (event.getSource() == deleteQuery)
			{
				//Update the save file
				try
				{
					querySaver.deleteSavedQuery(savedQueries.getSelectedIndex());

					//disable the delete button if there are no saved queries left
					if (savedQueriesModel.isEmpty())
					{
						deleteQuery.setEnabled(false);
					}
				}
				catch (IOException ex)
				{
					//TODO DEBUG
					ex.printStackTrace();
					
					showErrorMessage("The query could not be deleted.");
					saveQuery.setEnabled(false);
				}
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
				queryText.setText(savedQueries.getSelectedValue());
			}
		}
	}
}
