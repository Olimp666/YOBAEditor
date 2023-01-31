module com.yobasoft.yobaeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires opencv;
    requires java.desktop;
    requires com.google.gson;
    requires org.hildan.fxgson;

    opens com.yobasoft.yobaeditor to javafx.fxml, org.hildan.fxgson, com.google.gson;
    exports com.yobasoft.yobaeditor;
}