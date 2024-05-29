call mvn -f C:\source\gltf-io\java\pom.xml clean compile install -DskipTests
call mvn -f C:\source\gltf-imageio\java\pom.xml clean compile install -DskipTests
call mvn clean compile install -DskipTests -Plwjgl-natives-linux-amd64
xcopy .\varg-lwjgl3\target\original-varg-lwjgl3-0.0.1-SNAPSHOT.jar ..\executable\vargviewer_linuxx64_0.3.jar /f /y
