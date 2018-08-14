package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
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


import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ModRegCont implements Initializable {
    //variables
    private int index=0;

    private Connection con=null;
    private ArrayList<Module>mods;
    private ObservableList<Module>obsMods;

    @FXML private JFXListView<Module> lstMods;
    @FXML private JFXTextField txtModCode;
    @FXML private JFXTextField txtModName;
    @FXML private JFXTextField txtLectCode;
    @FXML private JFXTextField txtLectName;
    @FXML private JFXTextField txtLectEmail;
    @FXML private Spinner<Integer> spnModLev;
    @FXML private JFXButton btnNewMod;
    @FXML private JFXButton btnDelMod;
    @FXML private JFXButton btnSaveMod;
    @FXML private JFXButton btnSaveLect;

    private ObservableList<Lecturer>obsLects;

    @FXML private JFXListView<Lecturer>lstLects;
    @FXML private JFXTextField txtLectEditCode;
    @FXML private JFXTextField txtLectEditName;
    @FXML private JFXTextField txtLectEditEmail;
    @FXML private JFXButton btnLectEditSave;
    @FXML private JFXButton btnNewLect;
    @FXML private JFXButton btnDelLect;
    @FXML private JFXButton btnLogOut;

    //methods
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //modules
        mods=new ArrayList<Module>();
        obsMods=FXCollections.observableArrayList();
        obsMods.addAll(mods);
        setUpMods();

        lstMods.setItems(obsMods);

        lstMods.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if(oldValue!=null){
                txtModCode.textProperty().unbindBidirectional(oldValue.modCodeProperty());
                txtModName.textProperty().unbindBidirectional(oldValue.modNameProperty());

                //changes in lecturer
                connect();
                try {

                    String sql="select * from Lecturer where LectCode = ?";
                    PreparedStatement stmt=con.prepareStatement(sql);
                    stmt.setString(1,oldValue.lectCodeProperty().get());
                    ResultSet result=stmt.executeQuery();
                    while (result.next()){
                        String lectCode=result.getString("LectCode");
                        String lectName=result.getString("Name");
                        String lectEmail=result.getString("Email");
                        Lecturer curLect=new Lecturer(lectCode,lectName,lectEmail);
                        txtLectCode.textProperty().unbindBidirectional(curLect.lectCodeProperty());
                        txtLectName.textProperty().unbindBidirectional(curLect.lectNameProperty());
                        txtLectEmail.textProperty().unbindBidirectional(curLect.lectEmailProperty());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                disconnect();
            }
            if(newValue!=null){
                txtModCode.textProperty().bindBidirectional(newValue.modCodeProperty());
                txtModName.textProperty().bindBidirectional(newValue.modNameProperty());

                //changes in lecturer

                connect();
                try {
                    String sql="select * from Lecturer where LectCode = ?";
                    PreparedStatement stmt=con.prepareStatement(sql);
                    stmt.setString(1,newValue.lectCodeProperty().get());
                    ResultSet result=stmt.executeQuery();
                    while (result.next()){
                        String lectCode=result.getString("LectCode");
                        String lectName=result.getString("Name");
                        String lectEmail=result.getString("Email");
                        Lecturer curLect=new Lecturer(lectCode,lectName,lectEmail);
                        txtLectCode.textProperty().bindBidirectional(curLect.lectCodeProperty());
                        txtLectName.textProperty().bindBidirectional(curLect.lectNameProperty());
                        txtLectEmail.textProperty().bindBidirectional(curLect.lectEmailProperty());
                        spnModLev.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,3,newValue.modLevelProperty().getValue()));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                disconnect();
            }
        }));
        lstMods.getSelectionModel().selectFirst();

        btnSaveMod.setOnAction(event -> {
            connect();
            try {
                String sql="update Module set ModName = ?, ModLevel = ? where ModCode = ?";
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,txtModName.getText());
                stmt.setInt(2,spnModLev.getValue());
                stmt.setString(3,txtModCode.getText());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
            refreshSelectedListItem();
        });
        btnSaveLect.setOnAction(event -> {
            connect();
            String sql="Update Lecturer set Name = ?, Email = ? where LectCode = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,txtLectName.getText());
                stmt.setString(2,txtLectEmail.getText());
                stmt.setString(3,txtLectCode.getText());
                stmt.executeUpdate();
                int index=lstMods.getSelectionModel().getSelectedIndex();
                lstMods.getItems().clear();
                setUpMods();
                lstMods.getSelectionModel().select(index);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        });
        btnNewMod.setOnAction(event -> {
            Parent addRoot = null;
            try {
                addRoot=FXMLLoader.load(getClass().getResource("newModule.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Scene newScene=new Scene(addRoot);
            Stage primeStage= (Stage) ((Node)event.getSource()).getScene().getWindow();
            Stage stage=new Stage();
            stage.initOwner(primeStage);
            stage.setScene(newScene);
            stage.setTitle("New Module");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setWidth(400.0);
            stage.setHeight(400.0);
            stage.showAndWait();
            obsMods.clear();
            setUpMods();
        });
        btnDelMod.setOnAction(event -> {
            Alert delAlert=new Alert(Alert.AlertType.CONFIRMATION);
            delAlert.setTitle("Module Delete");
            delAlert.setContentText("Are you sure you want to delete "+txtModName.textProperty().get()+" ?");
            Optional<ButtonType>result=delAlert.showAndWait();
            if(result.get()==ButtonType.OK){
                connect();
                String sql="Delete from Module where ModCode = ?";
                try {
                    PreparedStatement stmt=con.prepareStatement(sql);
                    stmt.setString(1,txtModCode.getText());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                disconnect();
                obsMods.remove(lstMods.getSelectionModel().getSelectedItem());
            }else{
                delAlert.close();
            }
        });
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

        //lecturers
        obsLects=FXCollections.observableList(new ArrayList<>());
        setUpLects();

        lstLects.setItems(obsLects);
        lstLects.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue!=null){
                txtLectEditCode.textProperty().unbindBidirectional(oldValue.lectCodeProperty());
                txtLectEditName.textProperty().unbindBidirectional(oldValue.lectNameProperty());
                txtLectEditEmail.textProperty().unbindBidirectional(oldValue.lectEmailProperty());
            }
            if(newValue!=null){
                txtLectEditCode.textProperty().bindBidirectional(newValue.lectCodeProperty());
                txtLectEditName.textProperty().bindBidirectional(newValue.lectNameProperty());
                txtLectEditEmail.textProperty().bindBidirectional(newValue.lectEmailProperty());
            }
        });
        lstLects.getSelectionModel().selectFirst();
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
            stage.setTitle("New Lecturer");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setWidth(400.0);
            stage.setHeight(400.0);
            stage.showAndWait();
            obsLects.clear();
            setUpLects();
        });
        btnDelLect.setOnAction(event -> {
            Alert delAlert=new Alert(Alert.AlertType.CONFIRMATION);
            delAlert.setTitle("Module Lecturer");
            delAlert.setContentText("Are you sure you want to delete "+txtLectEditName.textProperty().get()+" ?");
            Optional<ButtonType>result=delAlert.showAndWait();
            if(result.get()==ButtonType.OK){
                connect();
                String sql="Delete from Lecturer where LectCode = ?";
                try {
                    PreparedStatement stmt=con.prepareStatement(sql);
                    stmt.setString(1,txtLectEditCode.getText());
                    stmt.executeUpdate();
                    obsLects.remove(lstLects.getSelectionModel().getSelectedItem());
                } catch (SQLException e) {
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Delete Error");
                    alert.setHeaderText("Can't delete "+txtLectEditName.textProperty().getValue()+" : Lecturer is linked to a module");
                    alert.showAndWait();
                }
                disconnect();
            }else{
                delAlert.close();
            }
        });
        btnLectEditSave.setOnAction(event -> {
            connect();
            String sql="Update Lecturer set Name = ?, Email = ? where LectCode = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,txtLectEditName.getText());
                stmt.setString(2,txtLectEditEmail.getText());
                stmt.setString(3,txtLectEditCode.getText());
                stmt.executeUpdate();
                int index=lstLects.getSelectionModel().getSelectedIndex();
                lstLects.getItems().clear();
                setUpLects();
                lstLects.getSelectionModel().select(index);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        });

    }
    private void setUpMods(){
        connect();
        Statement stmt=null;
        try {
            stmt=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql="select * from Module";
        try {
            ResultSet result=stmt.executeQuery(sql);
            while (result.next()){
                String modCode=result.getString("ModCode");
                String modName=result.getString("ModName");
                Integer modLevel=result.getInt("ModLevel");
                String lectCode=result.getString("LectCode");
                Module newMod=new Module(modCode,modName,modLevel,lectCode);
                obsMods.add(newMod);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }
    private void refreshSelectedListItem() {
        int index = lstMods.getSelectionModel().getSelectedIndex();

        if (index >= 0) {

            lstMods.fireEvent(
                    new ListView.EditEvent<>(
                            lstMods,
                            ListView.editCommitEvent(),
                            obsMods.get(index),
                            index
                    )
            );

            // unfortunately, the event loses the currently selected item, so reselect it
            lstMods.getSelectionModel().select(index);
        }
    }

    private void setUpLects(){
        connect();
        Statement stmt=null;
        try {
            stmt=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql="select LectCode, Name, Email from Lecturer";
        try {
            ResultSet result=stmt.executeQuery(sql);
            while (result.next()){
                String lectCode=result.getString("LectCode");
                String lectName=result.getString("Name");
                String lectEmail=result.getString("Email");
                Lecturer newLect=new Lecturer(lectCode,lectName,lectEmail);
                obsLects.add(newLect);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
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
