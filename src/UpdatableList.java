import javax.swing.*;

public class UpdatableList<E extends Comparable<E>> extends JList<E>
{
	private DefaultListModel<E> listModel;
	private boolean isSorted; //A sorted list does not allow duplicates
	
	/**
	 * Creates an updatable JList
	 * 
	 * @param isSorted true if the list should be sorted
	 */
	public UpdatableList(boolean isSorted)
	{
		listModel = new DefaultListModel<E>();
		setModel(listModel);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		this.isSorted = isSorted;
	}
	
	/**
	 * Adds an element to this list.
	 * 
	 * @param obj the element to add
	 * @return true if the element was added to the list
	 */
	public boolean add(E obj)
	{
		if (isSorted)
		{
			int index = 0;
			while (index < listModel.getSize() && (listModel.get(index)).compareTo(obj) < 0)
			{
				index++;
			}
			
			//If index == size, query will be inserted at the end. Also don't insert duplicate queries.
			if (index == listModel.getSize() || !listModel.get(index).equals(obj))
			{
				listModel.add(index, obj);
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			listModel.addElement(obj);
			return true;
		}
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
	public DefaultListModel<E> getModel()
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
}
