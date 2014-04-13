package org.squirrelsql;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.squirrelsql.services.I18n;
import org.squirrelsql.services.Pref;
import org.squirrelsql.services.StageDimensionSaver;

public class Main extends Application
{

   private I18n i18n = new I18n(getClass());
   private Pref pref = new Pref(getClass());

   public static final String VERSION = "4fx";
   private SplitController _splitController;

   @Override
   public void start(Stage primaryStage) throws Exception
   {
      ExceptionHandler.initHandling();

      AppState.init(primaryStage, getParameters());

      primaryStage.setTitle(i18n.t("mainWin.title") + " " + VERSION);

      DockPaneChanel dockPaneChanel = new DockPaneChanel();

      _splitController = new SplitController(dockPaneChanel);



      BorderPane borderPane = new BorderPane();
      primaryStage.setScene(new Scene(borderPane));

      borderPane.setCenter(_splitController.getNode());



      DockButtonsCtrl dockButtonsCtrl = new DockButtonsCtrl(dockPaneChanel);
      Node dockButtons = dockButtonsCtrl.getNode();

      borderPane.setLeft(dockButtons);

      borderPane.setBottom(AppState.get().getStatusBarCtrl().getNode());

      final StageDimensionSaver dimensionSaver = new StageDimensionSaver("main", primaryStage, pref, 500d, 500d, null);

      adjustMessageSplit();



      primaryStage.setOnCloseRequest(windowEvent -> onClose(dimensionSaver));


      primaryStage.getIcons().add(new Props(getClass()).getImage("acorn.png"));

      primaryStage.show();

      Platform.runLater(() -> AppState.get().doAfterBootstrap());
   }

   private void adjustMessageSplit()
   {
      Runnable runnable = new Runnable()
      {
         public void run()
         {
            _splitController.adjustMessageSplit();
         }
      };

      Platform.runLater(runnable);
   }

   private void onClose(StageDimensionSaver dimesionSaver)
   {
      AppState.get().fireApplicationClosing();
      _splitController.close();
      dimesionSaver.save(); // Needed because we are going to exit
      Platform.exit();
      System.exit(0);
   }


   public static void main(String[] args)
   {
      launch(args);
   }
}
