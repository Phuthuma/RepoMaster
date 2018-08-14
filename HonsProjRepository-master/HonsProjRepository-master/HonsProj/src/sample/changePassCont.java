package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class changePassCont implements Initializable {
    //variables
    private Connection con;

    @FXML private JFXTextField txtUname;
    @FXML private JFXPasswordField passOld;
    @FXML private JFXPasswordField passNew;
    @FXML private JFXPasswordField passConf;
    @FXML private JFXButton btnSub;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setValidators();
        btnSub.setOnAction(event -> {
            DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder=fact.newDocumentBuilder();
                Document doc=builder.parse(new File("adminInfo.xml"));

                XPathFactory xpfac=XPathFactory.newInstance();
                XPath path=xpfac.newXPath();

                String query="/admin/username";
                String unameResult=path.evaluate(query,doc);

                String passQuery="/admin[password = '"+passOld.getText()+"']/password";
                String passResult=path.evaluate(passQuery,doc);

                if((txtUname.getText().matches(unameResult))&&(passResult.matches(passOld.getText()))){
                    Element rootElem=doc.getDocumentElement();
                    Element passElem= (Element) rootElem.getChildNodes().item(7);
                    passElem.setTextContent(passConf.getText());
                    saveDoc(doc,"adminInfo.xml");
                    Alert alert=new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Password Change Status");
                    alert.setHeaderText("Password change was successful");
                    alert.showAndWait();
                    return;
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //search lecturer database
            connect();
            String queryLect="select Password from lecturer where LectCode = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(queryLect);
                stmt.setString(1,txtUname.textProperty().get());
                ResultSet result=stmt.executeQuery();
                while (result.next()){
                    String pass=result.getString("Password");
                    if((pass.matches(passOld.getText()))&&(passNew.getText().matches(passConf.getText()))){
                        String updatePassQuery="update Lecturer set Password = ? where LectCode = ?";
                        PreparedStatement updateStmt=con.prepareStatement(updatePassQuery);
                        updateStmt.setString(1,passConf.getText());
                        updateStmt.setString(2,txtUname.getText());
                        updateStmt.executeUpdate();

                        Alert alert=new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Password Change Status");
                        alert.setHeaderText("Password change was successful");
                        alert.showAndWait();
                        return;
                    }else {
                        Alert alert=new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Incorrect Password");
                        alert.showAndWait();
                    }
                }

                //search student database
                String queryStud="select Password from Student where StudNo  = ?";
                stmt=con.prepareStatement(queryStud);
                stmt.setString(1,txtUname.getText());
                result=stmt.executeQuery();
                while (result.next()){
                    String pass=result.getString("Password");
                    if((pass.matches(passOld.getText()))&&(passNew.getText().matches(passConf.getText()))){
                        String updatePassQuery="update Student set Password = ? where StudNo = ?";
                        PreparedStatement updateStmt=con.prepareStatement(updatePassQuery);
                        updateStmt.setString(1,passConf.getText());
                        updateStmt.setString(2,txtUname.getText());
                        updateStmt.executeUpdate();

                        Alert alert=new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Password Change Status");
                        alert.setHeaderText("Password change was successful");
                        alert.showAndWait();
                        return;
                    }else {
                        Alert alert=new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Incorrect Password");
                        alert.showAndWait();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        });

    }

    public void setUname(String uname){
        txtUname.setText(uname);
    }
    private void setValidators(){
        RequiredFieldValidator reqVal=new RequiredFieldValidator();
        reqVal.setMessage("Required Field");
        txtUname.getValidators().add(reqVal);
        txtUname.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
                txtUname.validate();
        });
        passOld.getValidators().add(reqVal);
        passOld.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
                passOld.validate();
        });
        passNew.getValidators().add(reqVal);
        passNew.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)
                passNew.validate();
        });
        passConf.getValidators().add(reqVal);
        passConf.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue)passConf.validate();
        });
        passConf.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue){
                if(!passNew.getText().matches(passConf.getText())){
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Password confirmation error");
                    alert.setContentText("Password Miss-match!");
                    alert.showAndWait();
                }
            }

        });
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
                con=DriverManager.getConnection(connectionString, "solassistuser","Dfjf8d02fdjjJ");

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
