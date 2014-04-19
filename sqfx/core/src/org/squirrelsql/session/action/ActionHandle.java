package org.squirrelsql.session.action;

import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import org.squirrelsql.session.SessionTabContext;

public class ActionHandle
{
   private ActionConfiguration _actionConfiguration;
   private SessionTabContext _sessionTabContext;

   private SqFxActionListener _sqFxActionListener;
   private MenuItem _menuItem;
   private Button _toolbarButton;

   public ActionHandle(ActionConfiguration actionConfiguration, SessionTabContext sessionTabContext)
   {
      _actionConfiguration = actionConfiguration;
      _sessionTabContext = sessionTabContext;
   }

   public void setOnAction(SqFxActionListener sqFxActionListener)
   {
      _sqFxActionListener = sqFxActionListener;
   }

   private void actionPerformed()
   {
      if(null != _sqFxActionListener)
      {
         _sqFxActionListener.actionPerformed();
      }

   }

   public void setToolbarButton(Button toolbarButton)
   {
      _toolbarButton = toolbarButton;
      _toolbarButton.setOnAction((e) -> actionPerformed());
      _toolbarButton.setGraphic(_actionConfiguration.getIcon());
      _toolbarButton.setTooltip(new Tooltip(_actionConfiguration.getText() + "\t" + _actionConfiguration.getKeyCodeCombination()));

   }

   public ActionConfiguration getActionConfiguration()
   {
      return _actionConfiguration;
   }

   public void setDisable(boolean b)
   {
      if(null != _toolbarButton)
      {
         _toolbarButton.setDisable(b);
      }
      if(null != _menuItem)
      {
         _menuItem.setDisable(b);
      }
   }


   public SessionTabContext getSessionTabContext()
   {
      return _sessionTabContext;
   }

   public void setMenuItem(MenuItem menuItem)
   {
      _menuItem = menuItem;
      _menuItem.setOnAction((e) -> actionPerformed());
      _menuItem.setAccelerator(_actionConfiguration.getKeyCodeCombination());

   }

   public MenuItem getMenuItem()
   {
      return _menuItem;
   }

   public boolean matchesSessionContext(SessionTabContext sessionTabContext)
   {
      return _sessionTabContext.matches(sessionTabContext);
   }

   public boolean matchesPrimaryKey(ActionConfiguration actionConfiguration, SessionTabContext sessionTabContext)
   {
      return matchesSessionContext(sessionTabContext) && actionConfiguration.matches(actionConfiguration);
   }

   public void setActionScope(ActionScope actionScope)
   {
      setDisable(false == _actionConfiguration.getActionScope().equals(actionScope));
   }
}
