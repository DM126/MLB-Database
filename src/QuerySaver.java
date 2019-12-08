import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.DefaultListModel;

public class QuerySaver
{
	public static final String SAVED_QUERIES_FILENAME = "savedqueries.txt";
	
	private File savedQueriesFile;
	private SaveableList savedQueries; //The list of saved queries
	
	QuerySaver(SaveableList savedQueries)
	{
		this.savedQueries = savedQueries;
	}
	
	/**
	 * Loads the saved queries from a text file upon starting the application
	 * and writes them to a JList model.
	 * 
	 * @param savedQueriesModel the JList model to add the saved queries to
	 * @throws FileNotFoundException if savedqueries.txt could not be found
	 */
	public void loadSavedQueries() throws FileNotFoundException
	{
		savedQueriesFile = new File(SAVED_QUERIES_FILENAME);
		Scanner scan = new Scanner(savedQueriesFile);
		while (scan.hasNext())
		{
			savedQueries.add(scan.nextLine());
		}
		
		scan.close();
	}
	
	/**
	 * Saves the query currently in the text box.
	 * 
	 * @param savedQuery the query to save
	 * @return false if the query has already been saved
	 * @throws IOException if there was an error writing to the save file
	 */
	public boolean saveQuery(String savedQuery) throws IOException
	{	
		boolean wasAdded = savedQueries.add(savedQuery);
		
		if (wasAdded)
		{
			writeSavedQueriesToFile();
		}
		
		return wasAdded;
	}
	
	/**
	 * Deletes the currently selected saved query.
	 * 
	 * @param selectedIndex the index of the query to delete in the list
	 * @throws IOException if the save file could not be updated
	 */
	public void deleteSavedQuery(int selectedIndex) throws IOException
	{
		savedQueries.remove(selectedIndex);
		
		writeSavedQueriesToFile();
	}
	
	/**
	 * Writes the saved queries to the save file 
	 * 
	 * @throws FileNotFoundException if the file could not be found
	 */
	private void writeSavedQueriesToFile() throws FileNotFoundException
	{
		PrintWriter printer = new PrintWriter(savedQueriesFile);
		
		DefaultListModel<String> listModel = savedQueries.getModel();
		for (int i = 0; i < listModel.getSize(); i++)
		{
			printer.println(listModel.get(i));
		}
		
		printer.close();
	}
}
