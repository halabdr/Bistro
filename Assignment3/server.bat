cd /d "C:\Users\adham\Downloads\G8_Assignment3.zip\JAR"
java -Djava.library.path="G8_server_lib" --module-path "G8_server_lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "G8_server.jar;G8_server_lib/*" servergui.ServerApp 
