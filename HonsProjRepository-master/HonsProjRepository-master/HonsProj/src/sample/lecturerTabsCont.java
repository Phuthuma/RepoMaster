package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class lecturerTabsCont implements Initializable {
    //variables
    private Connection con=null;
    private String lectCode="?";

    private ArrayList<Module>mods;
    private ObservableList<Module> obsMods;

    @FXML private JFXListView<Module> lstMods;
    @FXML private JFXListView<Task> lstTasks;
    @FXML private Label lblCode;
    @FXML private JFXButton btnNewTask;
    @FXML private JFXButton btnDelTask;
    @FXML private JFXButton btnLogOut;

    //methods
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mods=new ArrayList<>();
        obsMods=FXCollections.observableList(mods);
        setUpMods();
        setUpTasks();
        lstMods.setItems(obsMods);
        lstMods.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue!=null){
                lstTasks.setItems(newValue.getObsTasks());
                lstTasks.getSelectionModel().selectFirst();
            }
        });
        lstMods.getSelectionModel().selectFirst();

        lstTasks.setOnMouseClicked(event -> {
            if((event.getButton()==MouseButton.PRIMARY)&&(event.getClickCount()==2)){
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("knowledgeGraph.fxml"));
                try {
                    Parent parent = loader.load();
                    knowledgeGraphCont cont = loader.<knowledgeGraphCont>getController();
                    cont.setLblTreeTitle(lstTasks.getSelectionModel().getSelectedItem().titleProperty().get());
                    cont.setLblTaskId(lstTasks.getSelectionModel().getSelectedItem().taskIdProperty().get());
                    cont.setUname(lblCode.getText());
                    cont.initialize(loader.getLocation(), loader.getResources());
                    Stage newStage = new Stage();
                    newStage.setTitle("Knowledge Graph");
                    newStage.setScene(new Scene(parent));
                    newStage.setMaximized(true);
                    Stage primeStage= (Stage) ((javafx.scene.Node)event.getSource()).getScene().getWindow();
                    primeStage.close();
                    newStage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnNewTask.setOnAction(event -> {
            if(lstMods.getSelectionModel().getSelectedItem()!=null) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("newTask.fxml"));
                try {
                    Parent parent = loader.load();
                    newTaskCont cont = loader.<newTaskCont>getController();
                    cont.setModCode(lstMods.getSelectionModel().getSelectedItem().modCodeProperty().get());
                    cont.initialize(loader.getLocation(), loader.getResources());
                    Stage newStage = new Stage();
                    newStage.setTitle("New Task");
                    newStage.setScene(new Scene(parent));
                    newStage.setHeight(400.0);
                    newStage.setWidth(400.0);
                    newStage.showAndWait();

                    //updating tasks after insertion
                    lstTasks.getItems().clear();
                    obsMods.clear();
                    setUpMods();
                    setUpTasks();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Select module in which you want to insert new task");
                alert.showAndWait();
            }
        });
        btnDelTask.setOnAction(event -> {
            String id=lstTasks.getSelectionModel().getSelectedItem().taskIdProperty().getValue();
            if(id!=null) {
                Alert delAlert = new Alert(Alert.AlertType.CONFIRMATION);
                delAlert.setTitle("Task Delete");
                delAlert.setContentText("Are you sure you want to delete " + id + " ?");
                Optional<ButtonType> result = delAlert.showAndWait();
                if (result.get() == ButtonType.OK) {

                    connect();
                    String sql = "Delete from Task where TaskCode = ?";
                    try {
                        PreparedStatement stmt = con.prepareStatement(sql);
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    disconnect();
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(new File("tasks.xml"));
                        delTaskFromXML(doc);
                        saveDoc(doc, "tasks.xml");
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (XPathExpressionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lstTasks.getItems().remove(lstTasks.getSelectionModel().getSelectedItem());


                } else {
                    delAlert.close();
                }
            }else {

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
    }

    private void setUpMods(){
        connect();
        String sql="select * from Module where LectCode = ?";
        try {
            PreparedStatement stmt=con.prepareStatement(sql);
            stmt.setString(1,lblCode.getText());
            ResultSet result=stmt.executeQuery();
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
    private void setUpTasks(){
        for (Module module:obsMods) {
            connect();
            String sql="select * from Task where ModCode = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,module.modCodeProperty().get());
                ResultSet result=stmt.executeQuery();
                while (result.next()){
                    String taskCode=result.getString("TaskCode");
                    Integer taskNo=result.getInt("TaskNo");
                    String title=result.getString("TaskTitle");
                    String modCode=result.getString("ModCode");
                    module.getObsTasks().add(new Task(taskCode,taskNo,title,modCode));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        }
    }
    public void setCode(String code){
        lblCode.setText(code);
    }
    private String getCode(){
        return lblCode.getText();
    }
    private void delTaskFromXML(Document doc) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        XPathFactory xpFact=XPathFactory.newInstance();
        XPath path=xpFact.newXPath();
        String query="//task[@id='"+lstTasks.getSelectionModel().selectedItemProperty().
                getValue().taskIdProperty().getValue()+"']";
        NodeList list= (NodeList) path.evaluate(query,doc,XPathConstants.NODESET);
        System.out.println("no of items: "+list.getLength());
        for(int i=0;i<list.getLength();i++){
            org.w3c.dom.Node curNode=list.item(i);
            if(curNode.getNodeType()== org.w3c.dom.Node.ELEMENT_NODE) {
                Element curElem=(Element)curNode;
                System.out.println("Node to be deleted name: "+curElem.getTagName()+" id: "+curElem.getAttribute("id"));
                doc.getDocumentElement().removeChild(curNode);
            }
        }
    }

    private void saveDoc(Document doc, String filename) throws Exception {
        // obtain serializer
        DOMImplementation impl = doc.getImplementation();
        DOMImplementationLS implLS = (DOMImplementationLS) impl.getFeature("LS", "3.0");
        LSSerializer ser = implLS.createLSSerializer();
        ser.getDomConfig().setParameter("format-pretty-print", true);

        // create file to save too
        FileOutputStream fout = new FileOutputStream(filename);

        // set encoding options
        LSOutput lsOutput = implLS.createLSOutput();
        lsOutput.setEncoding("UTF-8");

        // tell to save xml output to file
        lsOutput.setByteStream(fout);

        // FINALLY write output
        ser.write(doc, lsOutput);

        // close file
        fout.close();
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
