package com.example.konacno;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DualWindowDrawingApp extends Application {

    private Canvas canvas1;
    private Canvas canvas2;

    @Override
    public void start(Stage primaryStage) {
        // Kreiraj prvo platno
        canvas1 = new Canvas(400, 400);
        GraphicsContext gc1 = canvas1.getGraphicsContext2D();

        // Kreiraj drugo platno
        canvas2 = new Canvas(400, 400);
        GraphicsContext gc2 = canvas2.getGraphicsContext2D();

        // Praćenje pokreta miša i crtanje linija
        canvas1.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            gc1.beginPath();
            gc1.moveTo(event.getX(), event.getY());
            gc1.stroke();
            syncDrawing(gc2, event);
        });

        canvas1.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            gc1.lineTo(event.getX(), event.getY());
            gc1.stroke();
            syncDrawing(gc2, event);
        });

        // Postavljanje scene za prvi prozor
        StackPane root1 = new StackPane();
        root1.getChildren().add(canvas1);
        Scene scene1 = new Scene(root1, 400, 400);

        primaryStage.setTitle("Prozor 1 - Crtanje");
        primaryStage.setScene(scene1);
        primaryStage.show();

        // Kreiraj drugi prozor
        Stage secondaryStage = new Stage();
        StackPane root2 = new StackPane();
        root2.getChildren().add(canvas2);
        Scene scene2 = new Scene(root2, 400, 400);

        secondaryStage.setTitle("Prozor 2 - Prikaz crteža");
        secondaryStage.setScene(scene2);
        secondaryStage.show();
    }

    // Sinhronizacija crteža na drugom platnu
    private void syncDrawing(GraphicsContext gc, MouseEvent event) {
        gc.beginPath();
        gc.moveTo(event.getX(), event.getY());
        gc.lineTo(event.getX(), event.getY());
        gc.stroke();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

