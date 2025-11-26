module com.example.konacno {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.konacno to javafx.fxml;
    exports com.example.konacno;
    exports com.example.konacno.draw; // Dodaj ovu liniju
}