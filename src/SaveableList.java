import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class SaveableList extends ListPanel
{
	private QuerySaver querySaver;
	private RightClickMenu rightClickMenu;
	
	public SaveableList(DatabasePanel parent, String title) throws FileNotFoundException
	{
		super(parent, title);
		
		querySaver = new QuerySaver(this);
		querySaver.loadSavedQueries();
		
		EventListener listener = new EventListener();
		list.addMouseListener(listener);
		rightClickMenu = new RightClickMenu(listener);
	}
	
	/**
	 * Adds the query to a sorted position in the list.
	 * 
	 * @return false if the query is already in the list
	 */
	@Override
	public boolean add(String query)
	{
		int index = 0;
		while (index < listModel.getSize() && (listModel.get(index)).compareTo(query) < 0)
		{
			index++;
		}
		
		//If index == size, query will be inserted at the end. Also don't insert duplicate queries.
		if (index == listModel.getSize() || !listModel.get(index).equals(query))
		{
			listModel.add(index, query);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Saves a query.
	 * 
	 * @param query the query to save
	 * @return true if the query saved successfully
	 * @throws IOException if there was an error saving the query
	 */
	public boolean saveQuery(String query) throws IOException
	{
		return querySaver.saveQuery(query);
	}
	
	/**
	 * Checks for right clicks on list items and displays a popup menu
	 * to delete items.
	 */
	private class EventListener implements ActionListener, MouseListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			if (event.getSource() == rightClickMenu.delete)
			{
				try
				{
					//Update the save file
					querySaver.deleteSavedQuery(list.getSelectedIndex());
				}
				catch (IOException ex)
				{
					parent.showErrorMessage("The query could not be deleted.");
				}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent event) 
		{
			//Checks for right click on a saved query
			if (event.getSource() == list && event.getButton() == MouseEvent.BUTTON3)
			{
				int index = list.locationToIndex(event.getPoint());
				if (index >= 0 && list.getCellBounds(index, index).contains(event.getPoint()))
				{
					list.setSelectedIndex(index);
					rightClickMenu.show(event.getComponent(), event.getX(), event.getY());
				}
			}
		}
		
		//Unused mouselistener methods
		@Override
		public void mouseClicked(MouseEvent event) {}
		@Override
		public void mouseEntered(MouseEvent event) {}
		@Override
		public void mouseExited(MouseEvent event) {}
		@Override
		public void mouseReleased(MouseEvent event) {}
	}
	
	/**
	 * Menu that pops up upon right clicking a query in the list
	 */
	private class RightClickMenu extends JPopupMenu
	{
		private JMenuItem delete;
		
		public RightClickMenu(EventListener listener)
		{
			delete = new JMenuItem("Delete query");
			delete.addActionListener(listener);
			
			this.add(delete);
		}
	}
}
