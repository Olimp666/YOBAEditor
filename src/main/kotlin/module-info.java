module com.yobasoft.yobaeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires opencv;
    requires java.desktop;

    opens com.yobasoft.yobaeditor to javafx.fxml;
    exports com.yobasoft.yobaeditor;
}