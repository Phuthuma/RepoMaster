package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class forgotPassCont implements Initializable {
    //variables
    Connection con=null;
    private Label lblUname=new Label();

    @FXML private JFXTextField txtInput;
    @FXML private JFXButton btnSend;

    //methods
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtInput.setText(lblUname.getText());
        btnSend.setOnAction(event -> {

           connect();
           String sql="select StudMail, Password " +
                      "from Student " +
                      "where StudNo = ? " +
                      "or StudMail = ?";
            try {
                PreparedStatement stmt=con.prepareStatement(sql);
                stmt.setString(1,txtInput.getText());
                stmt.setString(2,txtInput.getText());
                ResultSet result=stmt.executeQuery();

                if(result.next()){
                    String StudMail=result.getString("StudMail");
                    String pass=result.getString("Password");

                    HtmlEmail email=new HtmlEmail();

                    email.setHostName("smtp.gmail.com");
                    email.setSmtpPort(587);
                    email.setStartTLSEnabled(true);
                    email.setAuthentication("phuthumaloyisopetse@gmail.com","sweleba88");

                    try {
                        email.setFrom("phuthumaloyisopetse@gmail.com");
                        email.addTo(StudMail);
                        email.setSubject("SolAssist: Password Recovery");
                        email.setHtmlMsg("Hi user your password is: "+pass);
                        email.send();
                    } catch (EmailException e) {
                        e.printStackTrace();
                    }
                }else {
                    sql="select Email, Password " +
                            "from Lecturer " +
                            "where LectCode = ? " +
                            "or Email = ?";
                    stmt=con.prepareStatement(sql);
                    stmt.setString(1,txtInput.getText());
                    stmt.setString(2,txtInput.getText());
                    result=stmt.executeQuery();

                    if(result.next()){
                        String LectMail=result.getString("Email");
                        String pass=result.getString("Password");

                        HtmlEmail email=new HtmlEmail();

                        email.setHostName("smtp.gmail.com");
                        email.setSmtpPort(587);
                        email.setStartTLSEnabled(true);
                        email.setAuthentication("phuthumaloyisopetse@gmail.com","sweleba88");

                        try {
                            email.setFrom("phuthumaloyisopetse@gmail.com");
                            email.addTo(LectMail);
                            email.setSubject("SolAssist: Password Recovery");
                            email.setHtmlMsg("Hi user your password is: "+pass);
                            email.send();
                        } catch (EmailException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        //search admin xml
                        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                        try {
                            DocumentBuilder builder=factory.newDocumentBuilder();

                            File fin=new File("adminInfo.xml");
                            Document doc=builder.parse(fin);
                            XPathFactory xpFactory=XPathFactory.newInstance();
                            XPath path=xpFactory.newXPath();

                            String uname=path.evaluate("/admin/username",doc);
                            String adminMail=path.evaluate("/admin/email",doc);
                            String pass=path.evaluate("/admin/password",doc);


                            if((txtInput.getText().matches(uname))||(txtInput.getText().matches(adminMail)))
                            {
                                HtmlEmail email=new HtmlEmail();

                                email.setHostName("smtp.gmail.com");
                                email.setSmtpPort(587);
                                email.setStartTLSEnabled(true);
                                email.setAuthentication("phuthumaloyisopetse@gmail.com","sweleba88");

                                try {
                                    email.setFrom("phuthumaloyisopetse@gmail.com");
                                    email.addTo(adminMail);
                                    email.setSubject("SolAssist: Password Recovery");
                                    email.setHtmlMsg("Hi user your password is: "+pass);
                                    email.send();
                                } catch (EmailException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                //send error message
                                Alert alert=new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText("Invalid username or Email!");
                                alert.showAndWait();
                            }
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XPathExpressionException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Stage stage= (Stage) ((Node)event.getSource()).getScene().getWindow();
                stage.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            disconnect();
        });
    }
    public void setUname(String uname){
        lblUname.setText(uname);
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
