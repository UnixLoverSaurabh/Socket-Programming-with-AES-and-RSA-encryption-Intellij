package sample.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;

public class Students {

    private SimpleStringProperty subject;
    private Button button;

    public Students( Button button, String subject) {
        this.subject = new SimpleStringProperty(subject);
        this.button = button;
        this.button.setText(subject);
    }

    public String getSubject() { return subject.get(); }

    public void setSubject(String subject) {
        this.subject = new SimpleStringProperty(subject);
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public Button getButton() {
        return button;
    }
}