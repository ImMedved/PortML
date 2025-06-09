To run:
1. Go to portmanager-ui 
2. mvn clean package
3. cd ..
4. docker compose up --build
5. It 2nd cmd/terminal go to portmanager-ui and run:

java --module-path "C:\Java\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml -jar target/portmanager-ui-jar-with-dependencies.jar

https://gluonhq.com/products/javafx/

Amazon corretto 17+ sdk is preferred. Or run without path if it works.
https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html

Docker is needed. 
Java 17+ is needed. 
