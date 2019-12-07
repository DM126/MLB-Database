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
	//TODO HAVE A REFERENCE TO THE TABLE ITSELF?
	private DefaultListModel<String> savedQueriesModel; //The model for the list of saved queries
	
	QuerySaver(DefaultListModel<String> savedQueriesModel)
	{
		this.savedQueriesModel = savedQueriesModel;
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
			savedQueriesModel.addElement(scan.nextLine());
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
			writeSavedQueriesToFile();
			
			return true;
		}

		return false;
	}
	
	/**
	 * Deletes the currently selected saved query.
	 * 
	 * @param selectedIndex the index of the query to delete in the list
	 * @throws IOException if the save file could not be updated
	 */
	public void deleteSavedQuery(int selectedIndex) throws IOException
	{
		savedQueriesModel.remove(selectedIndex);
		
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
		
		for (int i = 0; i < savedQueriesModel.getSize(); i++)
		{
			printer.println(savedQueriesModel.get(i));
		}
		
		printer.close();
	}
}
