package sample.Model;

import javafx.beans.property.SimpleStringProperty;

public class StudentsFirstTable {

    private SimpleStringProperty teacher;

    public StudentsFirstTable(String teacher) {
        this.teacher = new SimpleStringProperty(teacher);
    }

    public void setTeacher(String teacher) {
        this.teacher.set(teacher);
    }

    public String getTeacher() {
        return teacher.get();
    }

    public SimpleStringProperty teacherProperty() {
        return teacher;
    }
}