package sample;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class studentTabsCont implements Initializable {
    //Variables
    @FXML private Label lblWelcome;
    @FXML private JFXButton btnLogOut;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnLogOut.setOnAction(event -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("logIn.fxml"));
            try {
                Parent parent = loader.load();
                logInCont cont=loader.getController();
                cont.initialize(loader.getLocation(), loader.getResources());
                Stage newStage = new Stage();
                newStage.setScene(new Scene(parent));
                newStage.setTitle("SignIn");
                newStage.setResizable(false);
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initStyle(StageStyle.DECORATED);
                newStage.setWidth(800.0);
                newStage.setHeight(800.0);
                Stage primeStage= (Stage) ((Node)event.getSource()).getScene().getWindow();
                primeStage.close();
                newStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //Methods
    public void setName(String name){
        lblWelcome.setText(lblWelcome.getText()+" "+name);
    }
}
