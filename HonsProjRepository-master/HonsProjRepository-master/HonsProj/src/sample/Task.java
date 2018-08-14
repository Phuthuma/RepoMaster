package sample;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Task {
    //variables
    private StringProperty taskId;
    private IntegerProperty taskNo;
    private StringProperty title;
    private StringProperty modId;
    private ObservableList<Node>nodes;

    //constructors
    public Task(String taskId,Integer taskNo, String title,String modId){
        this.taskId=new SimpleStringProperty(taskId);
        this.taskNo=new SimpleIntegerProperty(taskNo);
        this.title=new SimpleStringProperty(title);
        this.modId=new SimpleStringProperty(modId);
    }

    //methods
    @Override
    public String toString() {
        return String.format("%s: %s","Task"+taskNo.get(),title.get());
    }
    public StringProperty taskIdProperty() {
        return taskId;
    }
    public IntegerProperty taskNoProperty() {
        return taskNo;
    }
    public StringProperty titleProperty() {
        return title;
    }
    public StringProperty modIdProperty() {
        return modId;
    }
}
