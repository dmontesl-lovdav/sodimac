echo Comienza proceso de Ordenes de Compra Batch
C:
cd "C:\\Proyectos\\Local\\OrdenesCompraBatch"
set SPRING_CONFIG_LOCATION=C:\\Proyectos\\Local\\OrdenesCompraBatch\\ocbatch\\src\\main\\resources\\application.properties
 
java -Dlogging.config=C:/Proyectos/Local/OrdenesCompraBatch/ocbatch/src/main/resources/logback.xml -jar C:/Proyectos/Local/OrdenesCompraBatch/ocbatch/target/ocbatch-0.0.1-SNAPSHOT.jar
 
echo Termina proceso Ordenes de Compra Batch