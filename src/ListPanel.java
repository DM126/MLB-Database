import java.awt.Dimension;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel with a list and title
 */
public class ListPanel extends JPanel
{
	protected DatabasePanel parent;
	protected JList<String> list;
	protected DefaultListModel<String> listModel;
	
	/**
	 * Creates an panel with a list and title
	 * 
	 * @param title the title of the list panel
	 * @param isSorted true if the list should be sorted
	 */
	public ListPanel(DatabasePanel parent, String title)
	{
		this.parent = parent;
		
		listModel = new DefaultListModel<String>();
		list = new JList<String>();
		list.setModel(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new SelectionListener());
		
		JScrollPane scroll = new JScrollPane();
		scroll.setPreferredSize(new Dimension(300, 200));
		scroll.setViewportView(list);
		
		add(new JLabel(title));
		add(scroll);
		setPreferredSize(new Dimension(scroll.getPreferredSize().width + 10, scroll.getPreferredSize().height + 50));
		
	}
	
	/**
	 * Adds a query to the list at the last index
	 * 
	 * @param query the query to add
	 * @return true if the element was added to the list
	 */
	public boolean add(String query)
	{
		listModel.addElement(query);
		return true;
	}
	
	/**
	 * Removes the element at the specified index.
	 */
	public void remove(int index)
	{
		listModel.remove(index);
	}
	
	/**
	 * @return the model for this list.
	 */
	public DefaultListModel<String> getModel()
	{
		return listModel;
	}
	
	/**
	 * @return true if the list model contains no elements
	 */
	public boolean isEmpty()
	{
		return listModel.isEmpty();
	}
	
	/**
	 * Detects changes in the selected item in the list
	 * and changes the text in the query textbox.
	 */
	private class SelectionListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent event)
		{
			if (event.getSource() == list && !event.getValueIsAdjusting())
			{
				parent.setQueryText(list.getSelectedValue());
			}
		}
		
	}
}
