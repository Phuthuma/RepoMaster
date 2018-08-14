package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class newLectCont implements Initializable {
    //variables
    private Connection con=null;

    @FXML private JFXTextField txtCode;
    @FXML private JFXTextField txtName;
    @FXML private JFXTextField txtEmail;
    @FXML private JFXPasswordField passOne;
    @FXML private JFXButton btnSaveLect;
    @FXML private JFXButton btnCancelLect;

    //Initialize
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        RequiredFieldValidator val = new RequiredFieldValidator();
        val.setMessage("Required Field");
        txtCode.getValidators().add(val);
        txtCode.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue)
                txtCode.validate();
        });
        txtName.getValidators().add(val);
        txtName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue)
                txtName.validate();
        });
        txtEmail.getValidators().add(val);
        txtEmail.focusedProperty().addListener((observable, oldValue, newValue) -> {
            txtEmail.validate();
        });
        txtEmail.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue){
                if(!validEmail()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Email validation error");
                    alert.setContentText("You have entered the wrong email pattern");
                    alert.showAndWait();
                }
            }
        });
        passOne.getValidators().add(val);
        passOne.focusedProperty().addListener((observable, oldValue, newValue) -> {passOne.validate();});
        btnSaveLect.setOnAction(event -> {
            connect();
            String sql="select LectCode from Lecturer where LectCode = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,txtCode.getText());
                ResultSet result=stmt.executeQuery();
                if (result.next()){
                    String foundModCode=result.getString("LectCode");
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("DB Primary Key Error");
                    alert.setContentText(foundModCode+" already exists in list!");

                    alert.showAndWait();
                }

                else {
                    if(validEmail()){
                        sql = "insert into Lecturer values (?, ?,?,?)";
                        stmt = con.prepareStatement(sql);
                        stmt.setString(1, txtCode.getText());
                        stmt.setString(2, txtName.getText());
                        stmt.setString(3, txtEmail.getText());
                        stmt.setString(4, passOne.getText());
                        stmt.execute();

                        HtmlEmail email=new HtmlEmail();

                        email.setHostName("smtp.gmail.com");
                        email.setSmtpPort(587);
                        email.setStartTLSEnabled(true);
                        email.setAuthentication("phuthumaloyisopetse@gmail.com","sweleba88");

                        try {
                            email.setFrom("phuthumaloyisopetse@gmail.com");
                            email.addTo(txtEmail.getText());
                            email.setSubject("SollAssist Credentials");
                            email.setHtmlMsg("Hi "+txtName.getText()+" an account has been created in solAssist for " +
                                    "username:"+txtCode.getText()+" password: "+ passOne.getText());
                            email.send();
                        } catch (EmailException e) {
                            e.printStackTrace();
                        }

                        Stage stage= (Stage) ((Node)event.getSource()).getScene().getWindow();
                        stage.close();
                    }
                    else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Email validation error");
                        alert.setContentText("You have entered the wrong email pattern");
                        alert.showAndWait();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        });

        btnCancelLect.setOnAction(event -> {
            Stage stage= (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.close();
        });

    }

    private boolean validEmail(){
        Pattern p=Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._]*@[a-zA-Z0-9]+([.][a-zA-Z]+)+");
        Matcher m=p.matcher(txtEmail.getText());
        if (m.find() && m.group().equals(txtEmail.getText()))
            return true;
        return false;
    }
    private void connect(){
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(true){
            String connectionString="jdbc:sqlserver://postgrad.nmmu.ac.za;database=SolAssist";

            try {
                con=DriverManager.getConnection(connectionString,"solassistuser","Dfjf8d02fdjjJ");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void disconnect(){
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
