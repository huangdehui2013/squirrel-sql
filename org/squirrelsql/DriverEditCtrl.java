package org.squirrelsql;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.squirrelsql.services.Conversions;
import org.squirrelsql.services.I18n;
import org.squirrelsql.services.Pref;
import org.squirrelsql.services.StageDimensionSaver;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DriverEditCtrl
{
   private static final String PREF_LAST_CLASSPATH_DIR = "last.classpath.dir";


   private I18n _i18n = new I18n(this.getClass());
   private Pref _pref = new Pref(getClass());
   private DriverEditView _driverEditView;
   private final Stage _dialog;


   public DriverEditCtrl(SquirrelDriver squirrelDriver)
   {
      try
      {

         FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DriverEditView.fxml"), ResourceBundle.getBundle(getClass().getPackage().getName() + ".i18n"));
         Region parent = (Region) fxmlLoader.load();
         _driverEditView = fxmlLoader.getController();

         String title = _i18n.t("change.driver.title", squirrelDriver.getName());

         _driverEditView.lblChangeDriver.setText(title);

         _driverEditView.txtName.setText(squirrelDriver.getName());
         _driverEditView.txtUrl.setText(squirrelDriver.getUrl());
         _driverEditView.txtWebUrl.setText(squirrelDriver.getWebsiteUrl());
         _driverEditView.txtSelectedDriver.setText(squirrelDriver.getDriverClassName());

         _driverEditView.lstClasspath.setItems(FXCollections.observableList(new ArrayList(squirrelDriver.getJarFileNamesList())));

         _driverEditView.lstClasspath.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);



         initListeners();

         _dialog = new Stage();
         _dialog.initModality(Modality.WINDOW_MODAL);
         _dialog.setTitle(title);
         _dialog.initOwner(AppState.get().getPrimaryStage());
         _dialog.setScene(new Scene(parent));

         new StageDimensionSaver("driveredit", _dialog, _pref, parent.getPrefWidth(), parent.getPrefHeight(), _dialog.getOwner());

         _dialog.show();

      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   private void initListeners()
   {
      _driverEditView.btnDriverCPAdd.setOnAction((e) -> onDriverCPAdd());
      _driverEditView.btnDriverCPRemove.setOnAction((e) -> onDriverCPRemove());
      _driverEditView.btnDriverCPUp.setOnAction((e) -> onDriverCPUp());
   }



   private void onDriverCPUp()
   {
      ObservableList<Integer> selectedIndices = _driverEditView.lstClasspath.getSelectionModel().getSelectedIndices();

      if (0 == selectedIndices.size())
      {
         return;
      }

      int[] newSelectedIndices = moveUp(Conversions.toInts(selectedIndices.toArray(new Integer[selectedIndices.size()])));

      _driverEditView.lstClasspath.getSelectionModel().clearSelection();

      for (int newSelectedIndex : newSelectedIndices)
      {
         _driverEditView.lstClasspath.getSelectionModel().select(newSelectedIndex);
      }
      //_panel.tblOrder.scrollRectToVisible(_panel.tblOrder.getCellRect(newSelRows[0], 0, false));
   }

   private void onDriverCPRemove()
   {
      ObservableList selectedItems = _driverEditView.lstClasspath.getSelectionModel().getSelectedItems();

      _driverEditView.lstClasspath.getItems().removeAll(selectedItems.toArray(new Object[selectedItems.size()]));

   }

   public int[] moveUp(int[] toMoveUp)
   {
      for (int i : toMoveUp)
      {
         if (0 == i)
         {
            return toMoveUp;
         }
      }

      int[] newSelIndices = new int[toMoveUp.length];
      for (int i = 0; i < toMoveUp.length; ++i)
      {
         Object item = _driverEditView.lstClasspath.getItems().get(toMoveUp[i]);
         _driverEditView.lstClasspath.getItems().remove((int)toMoveUp[i]);
         newSelIndices[i] = toMoveUp[i] - 1;
         _driverEditView.lstClasspath.getItems().add(newSelIndices[i], item);
      }

      return newSelIndices;
   }



   private void onDriverCPAdd()
   {
      FileChooser fc = new FileChooser();

      fc.setTitle(_i18n.t("driver.select.classpath.entry"));

      String lastClasspathDir = _pref.getString(PREF_LAST_CLASSPATH_DIR, System.getProperty("user.home"));

      fc.setInitialDirectory(new File(lastClasspathDir));

      List<File> files = fc.showOpenMultipleDialog(_dialog);

      if(null == files || 0 == files.size())
      {
         return;
      }

      String lastDir = files.get(0).getParent();
      _pref.set(PREF_LAST_CLASSPATH_DIR, lastDir);

      _driverEditView.lstClasspath.getItems().addAll(FXCollections.observableArrayList(Conversions.toPathString(files)));
   }


}
