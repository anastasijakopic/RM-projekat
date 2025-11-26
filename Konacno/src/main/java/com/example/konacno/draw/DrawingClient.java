package com.example.konacno.draw;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class DrawingClient extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private GraphicsContext gc;
    private double lastX, lastY;
    private boolean isErasing = false;


    private String currentWord;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(600, 400); // Smanjen panel
        gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Text title = new Text("POGAĐALICA");
        title.setFont(Font.font(24));

        /*Text wordDisplay = new Text("Crtaš ovo: " + currentWord);
        wordDisplay.setFont(Font.font(18));*/

        TextField zaServer = new TextField();
        TextField textField = new TextField();
        zaServer.setPromptText("Data rijec");
        textField.setPromptText("Rjesenje:");
        Button confirmButton = new Button("Potvrdi");
        Button saljiServeru = new Button("Rijec");
        Button eraserButton = new Button("Gumica");
        Button pencilButton = new Button("Olovka");

        // akcija na dugme potvrdi
        confirmButton.setOnAction(e -> checkGuessiiic(textField.getText(),primaryStage));

        saljiServeru.setOnAction(e-> {
            String rijecKojuJeDobio = zaServer.getText();
            out.println("Rijec koju je dobio igrac: " + rijecKojuJeDobio);
            zaServer.clear();
        });


        //panel za unos i dugmad
        HBox inputPanel = new HBox(10, zaServer, saljiServeru, textField, confirmButton, eraserButton, pencilButton);
        inputPanel.setStyle("-fx-alignment: center; -fx-padding: 10;");

        //Postavljanje događaja
        canvas.setOnMousePressed(event -> {
            lastX = event.getX();
            lastY = event.getY();
        });

        canvas.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();
            if (isErasing) {
                out.println("ERASE:" + lastX + "," + lastY + "," + x + "," + y);
                gc.clearRect(lastX - 10, lastY - 10, 20, 20); // Brisanje u blizini kursora
            } else {
                out.println("LINE:" + lastX + "," + lastY + "," + x + "," + y);
                gc.strokeLine(lastX, lastY, x, y);
            }
            lastX = x;
            lastY = y;
        });

        // Dugmad za gumicu i olovku
        eraserButton.setOnAction(e -> {
            isErasing = true;
            gc.setStroke(javafx.scene.paint.Color.WHITE); // Postavi boju gumice na belu
        });

        pencilButton.setOnAction(e -> {
            isErasing = false;
            gc.setStroke(javafx.scene.paint.Color.BLACK); // Vratite na crnu boju
        });

        root.setTop(title);
        root.setCenter(canvas);
        root.setBottom(inputPanel);
        //root.setRight(wordDisplay);

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("Crtačka Aplikacija");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
        new Thread(this::listenForServerMessages).start();
    }



    private void checkGuessiiic(String guess, Stage primaryStage) {
        out.println("Pokusaj: " + guess); // Pošalji pokušaj serveru
        new Thread(() -> checkGuess(guess, primaryStage)).start();
    }

    private void checkGuess(String guess, Stage primaryStage) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Tacno ili netacno: " + message);
                if (message.equalsIgnoreCase("TACNO")) {
                    // Otvaranje GUI prozora unutar JavaFX niti
                    Platform.runLater(() -> showCongratulations(primaryStage));
                } else if (message.equalsIgnoreCase("NETACNO")) {
                    Platform.runLater(() -> showItsWrongAnswer(primaryStage));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showCongratulations(Stage primaryStage) {
        Stage congratsStage = new Stage();
        Text congratsText = new Text("ČESTITAM POGODILI STE!");
        Button backButton = new Button("Nazad");

        backButton.setOnAction(e -> congratsStage.close());

        BorderPane congratsLayout = new BorderPane();
        congratsLayout.setCenter(congratsText);
        congratsLayout.setBottom(backButton);

        Scene congratsScene = new Scene(congratsLayout, 300, 200);
        congratsStage.setTitle("Čestitka");
        congratsStage.setScene(congratsScene);
        congratsStage.show();
    }

    private void showItsWrongAnswer(Stage primaryStage) {
        Stage wrongStage = new Stage();
        Text congratsText = new Text("POGREŠNO...POKUŠAJTE PONOVO!");
        Button backButton = new Button("Nazad");

        System.out.println("ovdjeeee");

        backButton.setOnAction(e -> wrongStage.close());

        BorderPane wrongLayout = new BorderPane();
        wrongLayout.setCenter(congratsText);
        wrongLayout.setBottom(backButton);

        Scene congratsScene = new Scene(wrongLayout, 300, 200);
        wrongStage.setTitle("Netacan odgovor!");
        wrongStage.setScene(congratsScene);
        wrongStage.show();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForServerMessages() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                //System.out.println("Porukica: " + message);
                processMessage(message);
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message) {
        String[] parts = message.split(":");
        if (parts[0].equals("LINE")) {
            String[] coords = parts[1].split(",");
            double x1 = Double.parseDouble(coords[0]);
            double y1 = Double.parseDouble(coords[1]);
            double x2 = Double.parseDouble(coords[2]);
            double y2 = Double.parseDouble(coords[3]);
            gc.strokeLine(x1, y1, x2, y2);
        } else if (parts[0].equals("ERASE")) {
            String[] coords = parts[1].split(",");
            double x1 = Double.parseDouble(coords[0]);
            double y1 = Double.parseDouble(coords[1]);
            double x2 = Double.parseDouble(coords[2]);
            double y2 = Double.parseDouble(coords[3]);
            gc.clearRect(x1 - 10, y1 - 10, 20, 20); // Brisanje u blizini
        }
    }
}