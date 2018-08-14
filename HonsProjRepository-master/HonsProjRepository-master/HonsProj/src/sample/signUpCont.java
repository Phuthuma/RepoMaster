package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signUpCont implements Initializable {
    //variables
    private Connection con=null;
    @FXML private JFXTextField txtStudNo;
    @FXML private JFXTextField txtName;
    @FXML private JFXTextField txtEmail;
    @FXML private JFXTextField txtCourse;
    @FXML private JFXPasswordField txtPass;
    @FXML private JFXPasswordField txtPassCon;
    @FXML private JFXButton btnSignUp;
    @FXML private JFXButton btnCancel;


    //methods
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setValidators();
        btnSignUp.setOnAction(event -> {
            connect();
            String sql="select StudNo  from Student where StudNo = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,txtStudNo.getText());
                ResultSet result=stmt.executeQuery();
                if (result.next()){
                    String foundStudNo=result.getString("StudNo");
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("DB Primary Key Error");
                    alert.setContentText(foundStudNo+" already exists in list!");
                    alert.showAndWait();
                }
                else {
                    if((txtPass.getText().matches(txtPassCon.getText()))&&(validEmail())) {
                        sql = "insert into Student values (?,?,?,?,?)";
                        stmt = con.prepareStatement(sql);
                        stmt.setString(1, txtStudNo.getText());
                        stmt.setString(2, txtName.getText());
                        stmt.setString(3, txtEmail.getText());
                        stmt.setString(4, txtCourse.getText());
                        stmt.setString(5, txtPass.getText());
                        try {
                            stmt.execute();
                        }catch (Exception e){
                            //write error message
                            Alert alert=new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Email Validation Error");
                            alert.setContentText(txtEmail.getText()+" already exists in list!");
                            alert.showAndWait();
                        }


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
                                    "username:"+txtStudNo.getText()+" password: "+ txtPass.getText());
                            email.send();
                        } catch (EmailException e) {
                            e.printStackTrace();
                        }

                        //Stage stage= (Stage) ((Node)event.getSource()).getScene().getWindow();
                        //stage.close();
                    }else {
                        if(!txtPass.getText().matches(txtPassCon.getText())) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Password confirmation error");
                            alert.setContentText("Password Miss-match!");
                            alert.showAndWait();
                        }
                        if(!validEmail()){
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Email validation error");
                            alert.setContentText("You have entered the wrong email pattern");
                            alert.showAndWait();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        });
        btnCancel.setOnAction(event -> {
            Parent addRoot = null;
            try {
                addRoot=FXMLLoader.load(getClass().getResource("signUp.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage primeStage= (Stage) ((Node)event.getSource()).getScene().getWindow();
            primeStage.close();
        });
    }

    private void setValidators(){
        RequiredFieldValidator reqVal=new RequiredFieldValidator();
        reqVal.setMessage("Required Field");
        txtStudNo.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
            {
                txtStudNo.getValidators().add(reqVal);
                txtStudNo.validate();
            }
        });
        txtName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
            {
                txtName.getValidators().add(reqVal);
                txtName.validate();
            }
        });
        txtEmail.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
            {
                txtEmail.getValidators().add(reqVal);
                txtEmail.validate();
            }
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
        txtCourse.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
            {
                txtCourse.getValidators().add(reqVal);
                txtCourse.validate();
            }
        });
        txtPass.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
            {
                txtPass.getValidators().add(reqVal);
                txtPass.validate();
            }
        });
        txtPassCon.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
            {
                txtPassCon.getValidators().add(reqVal);
                txtPassCon.validate();
            }
        });
        txtPassCon.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue){
                if(!txtPass.getText().matches(txtPassCon.getText())){
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Password confirmation error");
                    alert.setContentText("Password Miss-match!");
                    alert.showAndWait();
                }
            }
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
