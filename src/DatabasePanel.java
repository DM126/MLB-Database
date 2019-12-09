import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;

public class DatabasePanel extends JPanel
{	
	private Connection conn;
	private Statement stmt;
	private JComboBox<String> tableSelect; //Selects which table to display
	private JTextArea queryText; //Where the user will enter a query
	private JButton executeQuery;
	private JButton saveQuery;
	private ListPanel historyPanel; //List of query history
	private SaveableList savedPanel; //List of saved queries
	private TableRowSorter<TableModel> tableSorter;
	private JTable table;

	/**
	 * Sets up the panel by creating the components and adding them to the 
	 * panel.
	 */
	public DatabasePanel(Connection conn)
	{
		try
		{
			this.conn = conn;
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			table = new JTable();
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			table.setEnabled(false);
			
			//For sorting the table by columns
			tableSorter = new TableRowSorter<TableModel>();
			table.setRowSorter(tableSorter);
			
			JScrollPane tableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			queryText = new JTextArea();
			queryText.setLineWrap(true);
			JScrollPane queryScroll = new JScrollPane(queryText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			queryScroll.setPreferredSize(new Dimension(500, 80));
			
			executeQuery = new JButton("Execute Query");
			
			saveQuery = new JButton("Save Query");
			
			JPanel queryPanel = new JPanel();
			queryPanel.add(new JLabel("Enter a query"));
			queryPanel.add(queryScroll);
			queryPanel.add(executeQuery);
			queryPanel.add(saveQuery);
			queryPanel.setPreferredSize(new Dimension(queryScroll.getPreferredSize().width + 20, queryScroll.getPreferredSize().height + executeQuery.getPreferredSize().height + 50));
			
			createTableComboBox();
			
			//Add the event listeners
			EventListener listener = new EventListener();
			tableSelect.addActionListener(listener);
			executeQuery.addActionListener(listener);
			saveQuery.addActionListener(listener);
			
			historyPanel = new ListPanel(this, "History");
			savedPanel = new SaveableList(this, "Saved queries");
			
			JPanel optionsPanel = new JPanel();
			optionsPanel.add(queryPanel);
			optionsPanel.add(historyPanel);
			optionsPanel.add(savedPanel);
			
			JPanel tableSelectPanel = new JPanel();
			tableSelectPanel.add(new JLabel("Select a table:"));
			tableSelectPanel.add(tableSelect);
			tableSelectPanel.setPreferredSize(new Dimension(queryPanel.getPreferredSize().width + historyPanel.getPreferredSize().width + savedPanel.getPreferredSize().width + 10, 50));
			
			optionsPanel.add(tableSelectPanel);
			optionsPanel.setPreferredSize(new Dimension(tableSelectPanel.getPreferredSize().width, historyPanel.getPreferredSize().height + tableSelectPanel.getPreferredSize().height + 10));
			
			setLayout(new BorderLayout());
			add(optionsPanel, BorderLayout.NORTH);
			add(tableScroll, BorderLayout.CENTER);
			
			//Populate the table with a sample query
			refreshTable("SELECT * FROM people;");
		} 
		catch (SQLException ex)
		{
			showErrorMessage("An error was encountered while trying to connect to the database.");
			System.exit(1);
		}
		catch (FileNotFoundException ex)
		{
			saveQuery.setEnabled(false);
			showErrorMessage("Could not load saved queries:\n" + QuerySaver.SAVED_QUERIES_FILENAME + " Could not be found.");
		}
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
	 * Updates the JTables of the panel with data from the database.
	 * 
	 * @param table the JTable to update
	 * @param query the query to execute
	 */
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
		
		historyPanel.add(query);
	}
	
	/**
	 * Displays a dialog box with an error message to the user then exit the program.
	 * 
	 * @param message the message to show.
	 */
	public void showErrorMessage(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Sets the text of the query text area.
	 * 
	 * @param newQuery the new query to write
	 */
	public void setQueryText(String newQuery)
	{
		queryText.setText(newQuery);
	}
	
	private class EventListener implements ActionListener
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
					showErrorMessage("There was an error reading the table.\n" + ex.getMessage());
				}
			}
			else if (event.getSource() == saveQuery) //button
			{
				if (queryText.getText() != "")
				{
					try
					{
						if (!savedPanel.saveQuery(queryText.getText()))
						{
							showErrorMessage("This query has already been saved.");
						}
					}
					catch (IOException ex)
					{
						showErrorMessage("The query could not be saved.");
						saveQuery.setEnabled(false);
					}
				}
				else
				{
					showErrorMessage("Query must not be empty.");
				}
			}
		}
	}
}
