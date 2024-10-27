package core.ui;

import core.ClientMain;
import core.localization.Localization;
import core.logging.Console;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateWindow {

    Scene scene;
    ProgressBar progressBar;

    public UpdateWindow(Stage primaryStage) {
        scene = new Scene(createPane(), 215, 115);
        primaryStage.setTitle(Localization.get("ui.update"));
    }

    private BorderPane createPane() {
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10, 10, 10, 10));

        Text updateText = new Text(Localization.get("ui.update_available"));
        borderPane.setTop(updateText);

        progressBar = new ProgressBar(0f);
        progressBar.prefWidthProperty().bind(borderPane.widthProperty());
        borderPane.setCenter(progressBar);

        Button updateButton = new Button(Localization.get("ui.update"));
        updateButton.prefWidthProperty().bind(borderPane.widthProperty());
        updateButton.setOnAction(event -> downloadUpdate());
        borderPane.setBottom(updateButton);

        return borderPane;
    }

    private void downloadUpdate() {
        SimpleFloatProperty progressProperty = new SimpleFloatProperty(0);
        Downloader downloader = new Downloader(progressProperty);
        progressBar.progressProperty().bind(progressProperty);
        new Thread(downloader).start();
    }

    public Scene getScene() {
        return scene;
    }

    private class Downloader implements Runnable {

        private FloatProperty progress;

        public Downloader(SimpleFloatProperty progressProperty) {
            this.progress = progressProperty;
        }

        //TODO: fix progress
        @Override
        public void run() {
            try {
                Console.info("Started download");
                URL url = new URL(ClientMain.DOWNLOAD_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                long fullFileSize = httpConnection.getContentLength();

                BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                FileOutputStream out = new FileOutputStream(ClientMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());

                final byte data[] = new byte[1024];
                int count;
                long currentSize = 0;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                    currentSize += count;

                    progress.set((currentSize / 3605650) * 100f);
                }

                in.close();
                out.close();

                try {
                    Runtime.getRuntime().exec("java -jar Client.jar");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                Console.err("Failed to download update");
            }
        }
    }

}
