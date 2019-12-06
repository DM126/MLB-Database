import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class QueryHistory<E> extends JList<E>
{
	//private ArrayList<E> queryHistory;
	private DefaultListModel<E> listModel;
	
	public QueryHistory()
	{
		//queryHistory = new ArrayList<E>();
		listModel = new DefaultListModel<E>();
		setModel(listModel);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public void add(E obj)
	{
		//queryHistory.add(obj);
		listModel.addElement(obj);
		//resetModel();
	}
	/*
	public void remove(E obj)
	{
		queryHistory.remove(obj);
		resetModel();
	}*/
	
	/**
	 * Resets the model for this JList
	 */
	/*private void resetModel()
	{
		DefaultListModel<E> model = new DefaultListModel<E>();
		for (E e : queryHistory)
		{
			model.addElement(e);
		}
		
		setModel(model);
	}*/
}
