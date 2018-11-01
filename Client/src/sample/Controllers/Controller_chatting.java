package sample.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import sample.Decryption.MessageDecryption;
import sample.Encryption.MessageEncryption;
import sample.Main;
import sample.Messages.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import sample.Messages.Table1;
import sample.Model.Students;
import sample.Model.StudentsFirstTable;
import sample.alertBox.AlertHelper;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller_chatting implements Initializable {

    public AnchorPane anchorpane;
    @FXML
    private Button loginButton, fileButton, exitButton, sendButton;

    @FXML
    private Label welcomeUsername;

    @FXML
    private TextField chatBox;

    @FXML
    private TableView<StudentsFirstTable> tbData1;

    @FXML
    public TableColumn<StudentsFirstTable, String> teacherColumn1;

    @FXML
    private TableView<Students> tbData2;

    @FXML
    public TableColumn<Students, String> subjectColumn2;

    private static ObservableList<StudentsFirstTable> Data1 = FXCollections.observableArrayList();

    private static ObservableList<Students> Data2 = FXCollections.observableArrayList();

    private Button[] button;

    private void setNumberOfButtons(int num) {
        button = new Button[num];
    }

    private void handleButtonAction(ActionEvent event) {
        for (int i = 0; i < button.length; i++) {
            if (event.getSource() == button[i]) {
                System.out.println(" Button " + (i + 1) + " Pressed");
            }
        }
    }

    @FXML
    private void buttonsActions(ActionEvent ev) throws IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        Button temp = (Button) ev.getSource();
        Window owner = chatBox.getScene().getWindow();

        if (temp == sendButton) {
            if (Main.sessionUsername == null) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "u r not logged in. plz login first");
                return;
            }

            String text = chatBox.getText();
            MessageEncryption mess = new MessageEncryption(text, Main.getKey());
            String cipherText = mess.getMessage();
            String from = "Client - " + Main.sessionUsername;
            Message message = new Message(cipherText, from, "Server");
            Main.getStringToEcho().writeObject("Message");
            Main.getStringToEcho().flush();
            Main.getStringToEcho().writeObject(message);
            Main.getStringToEcho().flush();
            System.out.println("AES encrypted message sent");
            series1.getData().add(new XYChart.Data(text.length(), mess.getDuration()));

        }
        if (temp == loginButton) {
            String uname = JOptionPane.showInputDialog(owner, "Enter Your Name: ");
            if (uname != null) {
                System.out.println(uname);
                Main.sessionUsername = uname;
                welcomeUsername.setText("Welcome " + Main.sessionUsername);
                fileButton.setDisable(false);
                clientChat();
            }
        }
        if (temp == fileButton) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"),
                    new FileChooser.ExtensionFilter("Zip", "*.zip"),
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );
            List<File> files = chooser.showOpenMultipleDialog(anchorpane.getScene().getWindow());
            if (files != null) {
                for (File file : files) {
                    System.out.println(file);
                    Main.getStringToEcho().writeObject("files");
                    Main.getStringToEcho().flush();
                    byte[] content = Files.readAllBytes(file.toPath());
                    MessageEncryption mess = new MessageEncryption(content, Main.getKey());
                    Main.getStringToEcho().writeObject(mess.getMessageImage());
                    Main.getStringToEcho().flush();
                    series1.getData().add(new XYChart.Data(content.length, mess.getDuration()));
                }
            }
        }
        if (temp == exitButton) {
            if (Main.getSocket() != null) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "u r logged out right now. ");
                logoutSession();
            }
            System.exit(0);
        }
    }

    private void clientChat() throws IOException {
        Main.getStringToEcho().writeObject("loginme");
        Main.getStringToEcho().flush();
        Main.getStringToEcho().writeObject(Main.sessionUsername);
        Main.getStringToEcho().flush();
        loginButton.setDisable(true);
    }

    private void logoutSession() {
        try {
            Main.getStringToEcho().writeObject("logoutme");
            Main.getStringToEcho().flush();
            Main.getStringToEcho().writeObject(Main.sessionUsername);
            Main.getStringToEcho().flush();
            Main.sessionUsername = null;
            loginButton.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertTab1(List<String> data1) {
        for (String aData1 : data1) {
            Data1.add(new StudentsFirstTable(aData1));
        }
    }

    private void insertTab2(List<String> data2) {
        //Data2.removeAll();
        //Data2.clear();
        for (int i = 0; i < data2.size(); i++) {
            Data2.add(new Students(button[i], data2.get(i)));
        }
    }

    private void loadTable1(ObservableList<StudentsFirstTable> studentsFirstTable) {
        teacherColumn1.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        //tbData1.refresh();
        tbData1.setItems(studentsFirstTable);
    }

    private void loadTable2(ObservableList<Students> student) {
        subjectColumn2.setCellValueFactory(new PropertyValueFactory<>("button"));
        tbData2.setItems(student);
    }

    private Thread alwaysWaitToReadObject = new Thread() {
        public void run() {
            do {
                try {
                    System.out.println("Thread Chattng is waiting to read Object input ");
                    Object object = Main.getEchoes().readObject();
                    System.out.println(Main.getEchoes() + "****" + String.valueOf(object.getClass()));
                    switch (String.valueOf(object.getClass())) {
                        case "class sample.Messages.Table1":
                            Table1 table1 = (Table1) object;
                            //System.out.println("Received server input for message Update: ");
                            List<String> data1 = table1.getData1();
                            insertTab1(data1);
                            loadTable1(Data1);
                            //System.out.println("Messages updated");
                            break;
                        case "class sample.Messages.Message":
                            Message messageServer = (Message) object;
                            MessageDecryption messDec = new MessageDecryption(messageServer.getMessage(), Main.getKey());
                            System.out.println(messDec.getMessage() + " FROM " + messageServer.getFrom());
                            break;
                        case "class java.lang.String":
                            String broadcastMessage = (String) object;
                            MessageDecryption messDecString = new MessageDecryption(broadcastMessage, Main.getKey());
                            List<String> tempData1 = new ArrayList<>();
                            tempData1.add(messDecString.getMessage());
                            insertTab1(tempData1);
                            loadTable1(Data1);
                            System.out.println("Broadcast message is " + messDecString.getMessage());
                            series2.getData().add(new XYChart.Data(messDecString.getMessage().length(), messDecString.getDuration()));
                            break;
                        case "class java.util.ArrayList":
                            System.out.println("Received server input for online users Update: ");
                            System.out.println(object);
                            List<String> data2 = (List<String>) object;
                            System.out.println(data2);
                            setNumberOfButtons(data2.size());
                            for (int i = 0; i < button.length; i++) {
                                button[i] = new Button();
                                //button[i].setOnAction(this::handleButtonAction);
                            }
                            insertTab2(data2);
                            System.out.println("This is data2" + data2);
                            loadTable2(Data2);
                            break;
                        default:
                            System.out.println("Default class for message readObject " + object.getClass());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error in receving chatting readObject");
                    e.printStackTrace();
                }
            } while (true);
        }
    };

    XYChart.Series series1 = new XYChart.Series();
    XYChart.Series series2 = new XYChart.Series();

    @FXML
    private void showGraph() {
        Stage stage = new Stage();
        stage.setTitle("Line Chart ");
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Length of Text");
        yAxis.setLabel("Time in nanoSecond");
        final LineChart<Number,Number> lineChart =
                new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setTitle("Duration statistics");

        series1.setName("Encryption (AES)");

        series2.setName("Decryption (AES)");


        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().addAll(series1, series2);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileButton.setDisable(true);
        alwaysWaitToReadObject.setDaemon(true); // terminate when main ends
        alwaysWaitToReadObject.start();
    }
}

