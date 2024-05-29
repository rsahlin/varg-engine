call mvn -f C:\source\gltf-io\java\pom.xml clean compile install -DskipTests
call mvn -f C:\source\gltf-imageio\java\pom.xml clean compile install -DskipTests
call mvn clean compile install -DskipTests -Plwjgl-natives-macos-amd64
xcopy .\varg-lwjgl3\target\original-varg-lwjgl3-0.0.1-SNAPSHOT.jar ..\executable\vargviewer_macx64_0.3.jar /f /y
