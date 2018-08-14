package sample;

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
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class newModuleCont implements Initializable {
    //variables
    private Connection con=null;

    private ArrayList<String>lectCodes;
    private ObservableList<String>obsLectCodes;

    @FXML private JFXTextField txtCode;
    @FXML private JFXTextField txtName;
    @FXML private Spinner<Integer>spnModLevel;
    @FXML private ComboBox<String>cmbLectCode;
    @FXML private Button btnSaveMod;
    @FXML private Button btnCancelMod;
    @FXML private Button btnNewLect;

    //methods
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        RequiredFieldValidator valReq=new RequiredFieldValidator();
        valReq.setMessage("Required field");
        txtCode.getValidators().add(valReq);

        txtCode.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue)
                txtCode.validate();
        });
        txtName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue){
                txtName.getValidators().add(valReq);
                txtName.validate();
            }
        });

        SpinnerValueFactory<Integer>factory=new SpinnerValueFactory.IntegerSpinnerValueFactory(1,3,1);
        spnModLevel.setValueFactory(factory);

        lectCodes=new ArrayList<>();
        obsLectCodes=FXCollections.observableList(lectCodes);
        setUpLectCodes();
        cmbLectCode.setItems(obsLectCodes);
        cmbLectCode.setPromptText("None");
        btnNewLect.setOnAction(event -> {
            Parent addRoot = null;
            try {
                addRoot=FXMLLoader.load(getClass().getResource("newLect.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Scene newScene=new Scene(addRoot);
            Stage primeStage= (Stage) ((Node)event.getSource()).getScene().getWindow();
            Stage stage=new Stage();
            stage.initOwner(primeStage);
            stage.setScene(newScene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("New Lecturer");
            stage.setWidth(400.0);
            stage.setHeight(400.0);
            stage.showAndWait();
            obsLectCodes.clear();
            setUpLectCodes();
        });
        btnSaveMod.setOnAction(event -> {
            connect();
            String sql="select ModCode from Module where ModCode = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,txtCode.getText());
                ResultSet result=stmt.executeQuery();
                if (result.next()){
                    String foundModCode=result.getString("ModCode");
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("DB Primary Key Error");
                    alert.setContentText(foundModCode+" already exists in list!");
                    alert.showAndWait();
                }
                else {
                    sql="insert into Module values (?, ?,?,?)";
                    stmt=con.prepareStatement(sql);
                    stmt.setString(1,txtCode.getText());
                    stmt.setString(2,txtName.getText());
                    stmt.setInt(3,spnModLevel.getValue());
                    stmt.setString(4,cmbLectCode.getSelectionModel().getSelectedItem());
                    stmt.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
            Stage stage= (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.close();

        });
        btnCancelMod.setOnAction(event -> {
            Stage stage= (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.close();
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
    private void setUpLectCodes(){
        connect();
        String sql="select LectCode from Lecturer";
        try {
            Statement stmt=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            ResultSet result=stmt.executeQuery(sql);
            while (result.next()){
                obsLectCodes.add(result.getString("LectCode"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }
}
