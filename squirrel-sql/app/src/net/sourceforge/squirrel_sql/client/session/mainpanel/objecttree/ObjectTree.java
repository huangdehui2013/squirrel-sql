package net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree;
/*
 * Copyright (C) 2002 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

import net.sourceforge.squirrel_sql.fw.gui.CursorChanger;
import net.sourceforge.squirrel_sql.fw.sql.BaseSQLException;
import net.sourceforge.squirrel_sql.fw.sql.DatabaseObjectInfo;
import net.sourceforge.squirrel_sql.fw.sql.IDatabaseObjectInfo;
import net.sourceforge.squirrel_sql.fw.sql.IDatabaseObjectTypes;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

import net.sourceforge.squirrel_sql.client.action.ActionCollection;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.client.session.action.DropSelectedTablesAction;
import net.sourceforge.squirrel_sql.client.session.action.RefreshObjectTreeAction;
import net.sourceforge.squirrel_sql.client.session.action.RefreshTreeItemAction;
/**
 * This is the tree showing the structure of objects in the database.
 *
 * @author  <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class ObjectTree extends JTree
{
	/** Logger for this class. */
	private static final ILogger s_log =
		LoggerController.createLogger(ObjectTree.class);

	/** Model for this tree. */
	private final ObjectTreeModel _model;

	/** Current session. */
	private final ISession _session;

	/**
	 * Collection of popup menus (<TT>JPopupMenu</TT> instances) for the
	 * object tree. Keyed by node type.
	 */
	private final Map _popups = new HashMap();

	/**
	 * Global popup menu. This contains items that are to be displayed
	 * in the popup menu no matter what items are selected in the tree.
	 */
	private final JPopupMenu _globalPopup = new JPopupMenu();

	private final List _globalActions = new ArrayList();

	/** This is a dummy node type that will <B>never</B> be in the object tree. */
	private final int _dummyNodeType;

	/**
	 * ctor specifying session.
	 * 
	 * @param	session	Current session.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>ISession</TT> passed.
	 */
	ObjectTree(ISession session)
	{
		super();
		if (session == null)
		{
			throw new IllegalArgumentException("ISession == null");
		}
		_session = session;

		_dummyNodeType = _session.getObjectTreeAPI().getNextAvailableNodeype();

		_model = new ObjectTreeModel(session);
		//		((BaseNode)_model.getRoot()).addBaseNodeExpandListener(this);
		//		_model.addTreeLoadedListener(this);
		//		_model.fillTree();

		addTreeExpansionListener(new NodeExpansionListener());

		setShowsRootHandles(true);
		setModel(_model);

//		refresh();

		// Add actions to the popup menu.
		ActionCollection actions = session.getApplication().getActionCollection();
		addToPopup(ObjectTreeNode.IObjectTreeNodeType.TABLE, actions.get(DropSelectedTablesAction.class));

		// Global menu.
		addToPopup(actions.get(RefreshObjectTreeAction.class));
		addToPopup(actions.get(RefreshTreeItemAction.class));

		// Mouse listener used to display popup menu.
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent evt)
			{
				if (evt.isPopupTrigger())
				{
					showPopup(evt.getX(), evt.getY());
				}
			}
			public void mouseReleased(MouseEvent evt)
			{
				if (evt.isPopupTrigger())
				{
					showPopup(evt.getX(), evt.getY());
				}
			}
		});
	}

	/**
	 * Component has been added to its parent.
	 */
	public void addNotify()
	{
		super.addNotify();
		// Register so that we can display different tooltips depending
		// which entry in list mouse is over.
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
	 * Component has been removed from its parent.
	 */
	public void removeNotify()
	{
		// Don't need tooltips any more.
		ToolTipManager.sharedInstance().unregisterComponent(this);
		super.removeNotify();
	}

	/**
	 * Return the name of the object that the mouse is currently
	 * over as the tooltip text.
	 *
	 * @param   event   Used to determine the current mouse position.
	 */
	public String getToolTipText(MouseEvent evt)
	{
		String tip = null;
		final TreePath path = getPathForLocation(evt.getX(), evt.getY());
		if (path != null)
		{
			tip = path.getLastPathComponent().toString();
		}
		else
		{
			tip = getToolTipText();
		}
		return tip;
	}

	/**
	 * Return the typed data model for this tree.
	 * 
	 * @return	The typed data model for this tree.
	 */
	public ObjectTreeModel getTypedModel()
	{
		return _model;
	}

	/**
	 * Refresh tree.
	 */
	public void refresh()
	{
		// TODO: Put in Johans code to save/restore state of tree.
//		TreePath[] paths = getSelectionPaths();
//		List l = _model.refresh();
//		if (l != null)
//		{
//			for (int i = 0, limit = l.size(); i < limit; ++i)
//			{
//				ObjectTreeNode node = (ObjectTreeNode)l.get(i);
//				node.addBaseNodeExpandListener(this);
//				TreePath path = new TreePath(_model.getPathToRoot(node));
//				expandPath(path);
//			}
//		}
//		setSelectionPaths(paths);

		ObjectTreeNode root = getTypedModel().getRootObjectTreeNode();
		root.removeAllChildren();
		expandNode(root, true);
	}

	private void expandNode(ObjectTreeNode parentNode, boolean selectParentNode)
	{
		if (parentNode == null)
		{
			throw new IllegalArgumentException("ObjectTreeNode == null");
		}

		// If node hasn't already been expanded.
		if (parentNode.getChildCount() == 0)
		{
			// Add together the standard expanders for this node type and any
			// individual expanders that there are for the node and process them.
			int nodeType = parentNode.getNodeType();
			INodeExpander[] stdExpanders = _model.getExpanders(nodeType);
			INodeExpander[] extraExpanders = parentNode.getExpanders();
			if (stdExpanders.length > 0 || extraExpanders.length > 0)
			{
				INodeExpander[] expanders = null;
				if (stdExpanders.length > 0 && extraExpanders.length == 0)
				{
					expanders = stdExpanders;
				}
				else if (stdExpanders.length == 0 && extraExpanders.length > 0)
				{
					expanders = extraExpanders;
				}
				else
				{
					expanders = new INodeExpander[stdExpanders.length + extraExpanders.length];
					System.arraycopy(stdExpanders, 0, expanders, 0, stdExpanders.length);
					System.arraycopy(extraExpanders, 0, expanders, stdExpanders.length,
										extraExpanders.length);
				}
				TreeLoader loader = new TreeLoader(parentNode, expanders,
													selectParentNode);
				_session.getApplication().getThreadPool().addTask(loader);
			}
		}
	}

	/**
	 * Add an item to the popup menu for the specified node type in the object
	 * tree.
	 * 
	 * @param	nodeType	Object Tree node type.
	 *						@see net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree.ObjectTreeNode.IObjectTreeNodeType
	 * @param	action		Action to add to menu.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> <TT>Action</TT> thrown.
	 */
	void addToPopup(int nodeType, Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Null Action passed");
		}
		JPopupMenu pop = getPopup(nodeType, true);
		pop.add(action);
	}

	/**
	 * Add an item to the popup menu for the all nodes.
	 * 
	 * @param	action		Action to add to menu.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> <TT>Action</TT> thrown.
	 */
	void addToPopup(Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Null Action passed");
		}
		_globalPopup.add(action);
		_globalActions.add(action);

		for (Iterator it = _popups.values().iterator(); it.hasNext();)
		{
			JPopupMenu pop = (JPopupMenu)it.next();
			pop.add(action);
		}
	}

	/**
	 * Get the popup menu for the passed node type. If one
	 * doesn't exist then create one if requested to do so.
	 */
	private JPopupMenu getPopup(int nodeType, boolean create)
	{
		Integer key = new Integer(nodeType);
		JPopupMenu pop = (JPopupMenu)_popups.get(key);
		if (pop == null && create)
		{
			pop = new JPopupMenu();
			_popups.put(key, pop);
			for (Iterator it = _globalActions.iterator(); it.hasNext();)
			{
				pop.add((Action)it.next());
			}
		}
		return pop;
	}

	/**
	 * Return an array of the currently selected nodes. This array is sorted
	 * by the simple name of the database object.
	 *
	 * @return	array of <TT>ObjectTreeNode</TT> objects.
	 */
	ObjectTreeNode[] getSelectedNodes()
	{
		TreePath[] paths = getSelectionPaths();
		List list = new ArrayList();
		if (paths != null)
		{
			for (int i = 0; i < paths.length; ++i)
			{
				Object obj = paths[i].getLastPathComponent();
				if (obj instanceof ObjectTreeNode)
				{
					list.add(obj);
				}
			}
		}
		ObjectTreeNode[] ar = (ObjectTreeNode[])list.toArray(new ObjectTreeNode[list.size()]);
		Arrays.sort(ar, new NodeComparator());
		return ar;
	}

	/**
	 * Return an array of the currently selected database
	 * objects.
	 *
	 * @return	array of <TT>ObjectTreeNode</TT> objects.
	 */
	IDatabaseObjectInfo[] getSelectedDatabaseObjects()
	{
		ObjectTreeNode[] nodes = getSelectedNodes();
		IDatabaseObjectInfo[] dbObjects = new IDatabaseObjectInfo[nodes.length];
		for (int i = 0; i < nodes.length; ++i)
		{
			dbObjects[i] = nodes[i].getDatabaseObjectInfo();
		}
		return dbObjects;
	}

	/**
	 * Get the appropriate popup menu for the currently selected nodes
	 * in the object tree and display it.
	 * 
	 * @param	x	X pos to display popup at.
	 * @param	y	Y pos to display popup at.
	 */
	private void showPopup(int x, int y)
	{
		ObjectTreeNode[] selObj = getSelectedNodes();
		if (selObj.length > 0)
		{
			// See if all selected nodes are of the same type.
			boolean sameType = true;
			final int nodeType = selObj[0].getNodeType();
			for (int i = 1; i < selObj.length; ++i)
			{
				if (selObj[i].getNodeType() != nodeType)
				{
					sameType = false;
					break;
				}
			}

			JPopupMenu pop = null; 
			if (sameType)
			{
				pop = getPopup(nodeType, false);
			}
			if (pop == null)
			{
				pop = _globalPopup;
			}
			pop.show(this, x, y);
		}
	}

	private final class NodeExpansionListener implements TreeExpansionListener
	{
		public void treeExpanded(TreeExpansionEvent evt)
		{
			// Get the node to be expanded.
			Object parentObj = evt.getPath().getLastPathComponent();
			if (parentObj instanceof ObjectTreeNode)
			{
				ObjectTree.this.expandNode((ObjectTreeNode)parentObj, false);
			}
		}

		public void treeCollapsed(TreeExpansionEvent evt)
		{
		}
	}

	/**
	 * This class is used to sort the nodes by their title.
	 */
	private static class NodeComparator implements Comparator
	{
		public int compare(Object obj1, Object obj2)
		{
			return obj1.toString().compareToIgnoreCase(obj2.toString());
		}
	}

	/**
	 * This class actually loads the tree.
	 */
	private final class TreeLoader implements Runnable
	{
		private ObjectTreeNode _parentNode;
		private INodeExpander[] _expanders;
		private boolean _selectParentNode;

		TreeLoader(ObjectTreeNode parentNode, INodeExpander[] expanders,
					boolean selectParentNode)
		{
			super();
			_parentNode = parentNode;
			_expanders = expanders;
			_selectParentNode= selectParentNode;
		}

		public void run()
		{
			try
			{
				synchronized (ObjectTree.this)
				{
					CursorChanger cursorChg = new CursorChanger(ObjectTree.this);
					cursorChg.show();
					try
					{
						ObjectTreeNode loadingNode = showLoadingNode();
						try
						{
							loadChildren();
						}
						finally
						{
							_parentNode.remove(loadingNode);
						}
					}
					finally
					{
						nodeStructureChanged(_parentNode);
						if (_selectParentNode)
						{
							clearSelection();
							setSelectionPath(new TreePath(_parentNode.getPath()));
						}
						cursorChg.restore();
					}
				}
			}
			catch (Throwable ex)
			{
				final String msg = "Error expanding: " + _parentNode.toString();
				s_log.error(msg, ex);
				_session.getMessageHandler().showMessage(msg + ": " + ex.toString());
			}
		}

		/**
		 * This adds a node to the tree that says "Loading..." in order to give
		 * feedback to the user.
		 */
		private ObjectTreeNode showLoadingNode()
		{
			IDatabaseObjectInfo doi = new DatabaseObjectInfo(null, null,
								"Loading...", IDatabaseObjectTypes.GENERIC_LEAF,
								_session.getSQLConnection());
			ObjectTreeNode loadingNode = new ObjectTreeNode(_session, doi);
			_parentNode.add(loadingNode);
			nodeStructureChanged(_parentNode);
			return loadingNode;
		}

		/**
		 * This expands the parent node and shows all its children.
		 */
		private void loadChildren() throws SQLException, BaseSQLException
		{
			for (int i = 0; i < _expanders.length; ++i)
			{
				boolean nodeTypeAllowsChildren = false;
				int lastNodeType = _dummyNodeType;
				List list = _expanders[i].createChildren(_session, _parentNode);
				Iterator it = list.iterator();
				while (it.hasNext())
				{
					Object nextObj = it.next();
					if (nextObj instanceof ObjectTreeNode)
					{
						ObjectTreeNode childNode = (ObjectTreeNode)nextObj;
						if (childNode.getExpanders().length >0)
						{
							childNode.setAllowsChildren(true);
						}
						else
						{
							int childNodeType = childNode.getNodeType();
							if (childNodeType != lastNodeType)
							{
								lastNodeType = childNodeType;
								if (_model.getExpanders(childNodeType).length > 0)
								{
									nodeTypeAllowsChildren = true;
								}
								else
								{
									nodeTypeAllowsChildren = false;
								}
							}
							childNode.setAllowsChildren(nodeTypeAllowsChildren);
						}
						_parentNode.add(childNode);
					}
				}
			}
		}

		/**
		 * Let the object tree model know that its structure has changed.
		 */
		private void nodeStructureChanged(ObjectTreeNode node)
		{
			ObjectTree.this._model.nodeStructureChanged(node);
		}
	}
}
