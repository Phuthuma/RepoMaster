package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class logInCont implements Initializable {
    //variables
    Connection con=null;

    private ArrayList<String>types;
    private ObservableList<String> obsTypes;

    @FXML private JFXTextField txtUname;
    @FXML private JFXComboBox<String>cmbType;
    @FXML private JFXPasswordField txtPass;
    @FXML private JFXButton btnSignIn;
    @FXML private JFXButton lblForgot;
    @FXML private JFXButton lblSignIn;
    @FXML private JFXButton lblChangePass;

    //methods
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        types=new ArrayList<>();
        obsTypes=FXCollections.observableList(types);
        obsTypes.add("Student");
        obsTypes.add("Lecturer");
        obsTypes.add("System Administrator");
        cmbType.setItems(obsTypes);

        setValidations();
        lblSignIn.setOnAction(event -> {
            Parent addRoot = null;
            try {
                addRoot=FXMLLoader.load(getClass().getResource("signUp.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Scene newScene=new Scene(addRoot);
            Stage primeStage= (Stage) ((Node)event.getSource()).getScene().getWindow();
            Stage stage=new Stage();
            stage.initOwner(primeStage);
            stage.setScene(newScene);
            stage.setTitle("SingUp");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setWidth(750.0);
            stage.setHeight(750.0);
            stage.showAndWait();
        });
        lblForgot.setOnAction(event -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("forgotPass.fxml"));
            try {
                Parent parent = loader.load();
                forgotPassCont cont=loader.getController();
                cont.setUname(txtUname.getText());
                cont.initialize(loader.getLocation(), loader.getResources());
                Stage newStage = new Stage();
                newStage.setScene(new Scene(parent));
                newStage.setTitle("Password Recovery");
                newStage.setResizable(false);
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initStyle(StageStyle.DECORATED);
                newStage.setWidth(750.0);
                newStage.setHeight(750.0);
                newStage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        btnSignIn.setOnAction(event -> {
            //student
            if(cmbType.getSelectionModel().getSelectedItem().matches(obsTypes.get(0))){

            }
            //lecturer
            else if(cmbType.getSelectionModel().getSelectedItem().matches(obsTypes.get(1))){
                connect();
                String sql="select Password from Lecturer where LectCode = ?";
                try {
                    PreparedStatement stmt=con.prepareStatement(sql);
                    stmt.setString(1,txtUname.getText());
                    ResultSet result=stmt.executeQuery();
                    if(result.next()){
                        String pass=result.getString("Password");
                        if(pass.matches(txtPass.getText())){
                            FXMLLoader loader=new FXMLLoader();
                            loader.setLocation(getClass().getResource("lecturerTabs.fxml"));
                            try {
                                Parent parent=loader.load();
                                lecturerTabsCont cont=loader.<lecturerTabsCont>getController();
                                cont.setCode(txtUname.getText());
                                cont.initialize(loader.getLocation(),loader.getResources());
                                Stage newStage=new Stage();
                                newStage.setScene(new Scene(parent));
                                newStage.setTitle("Modules");
                                newStage.setMaximized(true);
                                Stage primeStage= (Stage) ((Node)event.getSource()).getScene().getWindow();
                                primeStage.close();
                                newStage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else {
                            Alert alert=new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Invalid password!");
                            alert.showAndWait();
                        }
                    }else {
                        Alert alert=new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid Username!");
                        alert.showAndWait();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                disconnect();
            }
            //System Administrator
            else if(cmbType.getSelectionModel().getSelectedItem().matches(obsTypes.get(2))) {
                DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder=factory.newDocumentBuilder();
                    Document doc=builder.parse(new File("adminInfo.xml"));

                    Element rootElem=doc.getDocumentElement();
                    NodeList nodes=rootElem.getChildNodes();

                    System.out.println("No of Nodes: "+nodes.getLength());

                    Element unameElem= (Element) nodes.item(1);
                    //username found
                    if(unameElem.getTextContent().matches(txtUname.getText())){
                        Element passElem= (Element) nodes.item(7);
                        if(passElem.getTextContent().matches(txtPass.getText())){
                            FXMLLoader loader=new FXMLLoader();
                            loader.setLocation(getClass().getResource("modReg.fxml"));
                            try {
                                Parent parent=loader.load();
                                ModRegCont cont=loader.getController();
                                cont.initialize(loader.getLocation(),loader.getResources());
                                Stage newStage=new Stage();
                                newStage.setTitle("Module Register");
                                newStage.setScene(new Scene(parent));
                                newStage.setMaximized(true);
                                Stage primeStage= (Stage) ((Node)event.getSource()).getScene().getWindow();
                                primeStage.close();
                                newStage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Alert alert=new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Wrong password!");
                            alert.showAndWait();
                        }

                    }else {
                        Alert alert=new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid Username!");
                        alert.showAndWait();
                    }


                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //validation error
            else {

            }
        });
        lblChangePass.setOnAction(event -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ChangePass.fxml"));
            try {
                Parent parent = loader.load();
                changePassCont cont=loader.getController();
                //cont.setUname(txtUname.getText());
                cont.initialize(loader.getLocation(), loader.getResources());
                Stage newStage = new Stage();
                newStage.setScene(new Scene(parent));
                newStage.setTitle("Change Password");
                newStage.setResizable(false);
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.initStyle(StageStyle.DECORATED);
                newStage.setWidth(500.0);
                newStage.setHeight(500.0);
                newStage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private void setValidations(){
        RequiredFieldValidator val=new RequiredFieldValidator();
        val.setMessage("Required Field");
        txtUname.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue){
                txtUname.getValidators().add(val);
                txtUname.validate();
            }
        });
        txtPass.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue){
                txtPass.getValidators().add(val);
                txtPass.validate();
            }
        });
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
