package sample;

import com.sun.org.apache.xpath.internal.NodeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import org.w3c.dom.*;
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
import java.util.Optional;

public class TextFieldTreeCellImpl extends TreeCell<Node> {
    //Variables
    private TreeItem<Node>copyItem;
    private TextField txtField;
    private ContextMenu menu = new ContextMenu();
    private knowledgeGraphCont cont;

    //Constructors
    public TextFieldTreeCellImpl(TreeItem<Node>copyItem, String taskId,knowledgeGraphCont cont){
        this.copyItem=copyItem;
        this.cont=cont;

        //add menu item
        MenuItem addStud=new MenuItem("Add");
        addStud.setOnAction(event -> {
            Dialog dialog=new Dialog();
            dialog.setTitle("New Node");

            GridPane grid=new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20,150,10,10));

            TextField txtNo=new TextField();
            txtNo.textProperty().addListener((observable, oldValue, newValue) -> {
                if((newValue!=null)&&(!newValue.matches("\\d?"))){
                    Alert alert=new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Only numbers are accepted by this field!");
                    alert.showAndWait();
                }
            });
            txtNo.setPromptText("Enter Node No.");
            TextArea txtQues=new TextArea("");
            txtQues.setPromptText("Enter Question");
            TextArea txtAns=new TextArea("");
            txtAns.setPromptText("Enter Parent Answer");

            grid.add(new Label("Node No:"),0,0);
            grid.add(txtNo,1,0);
            grid.add(new Label("Question:"), 0, 1);
            grid.add(txtQues, 1, 1);
            grid.add(new Label("Parent Answer:"), 0, 2);
            grid.add(txtAns, 1, 2);


            dialog.getDialogPane().setContent(grid);

            ButtonType saveButtonType=new ButtonType("Save",ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType,ButtonType.CANCEL);

            Optional<ButtonType>result=dialog.showAndWait();
            if (result.get()==saveButtonType){
                //Add to xml file
                DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder=factory.newDocumentBuilder();
                    Document doc=builder.parse(new File("tasks.xml"));

                    XPathFactory xpFact=XPathFactory.newInstance();
                    XPath path=xpFact.newXPath();

                    String query="//task[@id='"+taskId+"']//node[@no = '"+getTreeItem().getValue().nodeNoProperty().getValue()+"' ]";
                    NodeList nodes= (NodeList) path.evaluate(query,doc,XPathConstants.NODESET);

                    System.out.println("No of Nodes: "+nodes.getLength());
                    for(int i=0;i<nodes.getLength();i++){
                        org.w3c.dom.Node curNode=nodes.item(i);
                        if(curNode.getNodeType()== org.w3c.dom.Node.ELEMENT_NODE){
                            Element curElem= (Element) curNode;
                            NodeList children=curElem.getChildNodes();

                            //we must add to child elem
                            Element childElem= (Element) children.item(5);

                            //create new node element
                            Element newElem=doc.createElement("node");
                            newElem.setAttribute("no",txtNo.getText());

                            Element quesElem=doc.createElement("question");
                            Text quesText=doc.createTextNode(txtQues.getText());
                            quesElem.appendChild(quesText);
                            newElem.appendChild(quesElem);

                            Element ansElem=doc.createElement("answer");
                            Text ansText=doc.createTextNode(txtAns.getText());
                            ansElem.appendChild(ansText);
                            newElem.appendChild(ansElem);

                            Element childrenElem=doc.createElement("children");
                            newElem.appendChild(childrenElem);

                            childElem.appendChild(newElem);
                            saveDoc(doc,"tasks.xml");
                            getTreeView().getRoot().getChildren().clear();
                            cont.setUpNodes();
                        }
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
            }

        });
        menu.getItems().add(addStud);

        //Copy menuItem
        MenuItem copyMenuItem=new MenuItem("Copy");
        copyMenuItem.setOnAction(event -> {
            copyItem.getChildren().clear();
            copyItem.setValue(getTreeView().getSelectionModel().getSelectedItem().getValue());
            copyItem.getChildren().addAll(getTreeView().getSelectionModel().getSelectedItem().getChildren());
        });
        menu.getItems().addAll(copyMenuItem);

        //Paste menuItem
        MenuItem pastItem=new MenuItem("Paste");
        pastItem.setOnAction(event -> {
            if (copyItem != null) {
                //add to xml file
                DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder=factory.newDocumentBuilder();
                    Document doc=builder.parse("tasks.xml");

                    XPathFactory xPathFac=XPathFactory.newInstance();
                    XPath path=xPathFac.newXPath();

                    String query="//task[@id='"+taskId+"']//node[@no = '"+getTreeItem().getValue().nodeNoProperty().getValue()+"' ]";

                    NodeList nodes= (NodeList) path.evaluate(query,doc,XPathConstants.NODESET);
                    for(int i=0;i<nodes.getLength();i++){
                        org.w3c.dom.Node curNode=nodes.item(i);
                        if(curNode.getNodeType()== org.w3c.dom.Node.ELEMENT_NODE){
                            Element curElem= (Element) curNode;
                            curElem.setAttribute("no",copyItem.getValue().nodeNoProperty().getValue().toString());

                            Element quesElem= (Element) curElem.getChildNodes().item(1);
                            quesElem.setTextContent(copyItem.getValue().questionProperty().getValue());

                            Element ansElem= (Element) curElem.getChildNodes().item(3);
                            ansElem.setTextContent(copyItem.getValue().answerProperty().getValue());

                            Element childrenElem= (Element) curElem.getChildNodes().item(5);
                            //traverse copy item children and add nodes
                            addChildNodes(doc,childrenElem,copyItem.getChildren());

                            System.out.println("Node Tag: "+curElem.getTagName()+" Node no: "+curElem.getAttribute("no"));
                        }
                    }
                    saveDoc(doc,"tasks.xml");
                    getTreeView().getRoot().getChildren().clear();
                    cont.setUpNodes();
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

            }else {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("There is no node copied to clipboard");
                alert.showAndWait();
            }
        });
        menu.getItems().add(pastItem);

        //Delete menuItem
        MenuItem delItem=new MenuItem("Delete");
        delItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Are you sure you want to delete node "+getTreeItem().getValue().nodeNoProperty().getValue());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                if(getTreeItem().equals(getTreeView().getRoot())){
                    getTreeView().getRoot().getValue().setNodeNo(0);
                    getTreeView().getRoot().getValue().setAnswer("");
                    getTreeView().getRoot().getValue().setQuestion("");
                    getTreeView().getRoot().getChildren().clear();
                    //build from here into the xml document
                    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                    Document doc=null;
                    try {
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        doc = builder.parse("tasks.xml");

                        //need to query delete element
                        XPathFactory xpFact=XPathFactory.newInstance();
                        XPath path=xpFact.newXPath();

                        String query="//task[@id='"+taskId+"']/node['1']";
                        NodeList rootNode= (NodeList) path.evaluate(query,doc,XPathConstants.NODESET);
                        NodeList list=rootNode.item(0).getChildNodes();

                        org.w3c.dom.Node quesNode=list.item(1);
                        Element quesElem= (Element) quesNode;
                        quesElem.setTextContent("");

                        org.w3c.dom.Node ansNode=list.item(3);
                        Element ansElem= (Element) ansNode;
                        ansElem.setTextContent("");

                        org.w3c.dom.Node childrenNode=list.item(5);
                        Element childrenElem= (Element) childrenNode;

                        while (childrenElem.hasChildNodes())
                            childrenElem.removeChild(childrenElem.getFirstChild());

                        saveDoc(doc,"tasks.xml");
                        getTreeView().getRoot().getChildren().clear();
                        cont.setUpNodes();

                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    getTreeView().getRoot().getChildren().remove(getTreeItem());
                    ObservableList<TreeItem<Node>>rootChildren=getTreeView().getRoot().getChildren();
                    //build from here into the xml document


                    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                    Document doc=null;
                    try {
                        DocumentBuilder builder=factory.newDocumentBuilder();
                        doc=builder.parse("tasks.xml");

                        XPathFactory xpFact=XPathFactory.newInstance();
                        XPath path=xpFact.newXPath();

                        String query="//task[@id='"+taskId+"']/node['1']";
                        NodeList list= (NodeList) path.evaluate(query,doc,XPathConstants.NODESET);


                        org.w3c.dom.Node curNode=list.item(0);
                        Element rootElem= (Element) curNode;

                        TreeItem<Node>rootItem=getTreeView().getRoot();

                        org.w3c.dom.Node quesNode=rootElem.getChildNodes().item(1);
                        Element quesElem= (Element) quesNode;
                        quesElem.setTextContent(rootItem.getValue().questionProperty().getValue());

                        org.w3c.dom.Node ansNode=rootElem.getChildNodes().item(3);
                        Element ansElem= (Element) ansNode;
                        ansElem.setTextContent(rootItem.getValue().questionProperty().getValue());

                        org.w3c.dom.Node childrenNode=rootElem.getChildNodes().item(5);
                        Element childrenElem= (Element) childrenNode;

                        //clearing xml file
                        while (childrenElem.hasChildNodes())
                            childrenElem.removeChild(childrenElem.getFirstChild());
                        saveDoc(doc,"tasks.xml");

                        //repopulate xml file
                        getTreeView().getRoot().getChildren().remove(getTreeItem());
                        addChildNodes(doc,childrenElem,getTreeView().getRoot().getChildren());

                        saveDoc(doc,"tasks.xml");
                        getTreeView().getRoot().getChildren().clear();
                        cont.setUpNodes();

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
                }

            } else {
                alert.close();
            }
        });
        menu.getItems().add(delItem);
    }

    //methods
    private void addChildNodes(Document doc, Element elem, ObservableList<TreeItem<Node>>treeItems){
        for (TreeItem<Node>curItem:treeItems) {
            Node curNode=curItem.getValue();

            Element newElem=doc.createElement("node");
            newElem.setAttribute("no",curNode.nodeNoProperty().getValue().toString());

            Element quesElement=doc.createElement("question");
            quesElement.setTextContent(curNode.questionProperty().getValue());
            newElem.appendChild(quesElement);

            Element ansElem=doc.createElement("answer");
            ansElem.setTextContent(curNode.answerProperty().getValue());
            newElem.appendChild(ansElem);

            Element childrenElem=doc.createElement("children");
            addChildNodes(doc,childrenElem,curItem.getChildren());
            newElem.appendChild(childrenElem);

            elem.appendChild(newElem);
        }
    }
    private static void saveDoc(Document doc, String filename) throws Exception {
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
    @Override
    public void startEdit() {
        super.startEdit();

        if(txtField==null)
            createTextField();
        setText(null);
        setGraphic(txtField);
        txtField.selectAll();
    }
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().toString());
        setGraphic(getTreeItem().getGraphic());
    }
    @Override
    protected void updateItem(Node item, boolean empty) {
        super.updateItem(item, empty);

        if(empty){
            setText(null);
            setGraphic(null);
        }else {
            if(isEditing()){
                if(txtField!=null){
                    txtField.setText(getNode().toString());
                }
                setText(null);
                setGraphic(txtField);
            }else {
                setText(getNode().toString());
                setGraphic(getTreeItem().getGraphic());
                if(getParent()!=null)
                    setContextMenu(menu);
            }
        }
    }

    //for selection

    private void createTextField(){
        txtField=new TextField(getNode().toString());
        txtField.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER) {
                commitEdit(new Node(Integer.parseInt(txtField.getText()),
                        getItem().questionProperty().get(),getItem().answerProperty().get()));
            }
            else if(event.getCode()==KeyCode.ESCAPE)
                cancelEdit();
        });
    }
    private Node getNode(){
        return getItem() == null? new Node(0):getItem();
    }
}
