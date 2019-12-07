import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class QueryHistory<E> extends JList<E>
{
	private DefaultListModel<E> listModel;
	
	public QueryHistory()
	{
		listModel = new DefaultListModel<E>();
		setModel(listModel);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public void add(E obj)
	{
		listModel.addElement(obj);
	}
}
