call mvn -f C:\source\gltf-io\java\pom.xml clean compile install -DskipTests
call mvn -f C:\source\gltf-imageio\java\pom.xml clean compile install -DskipTests
call mvn clean compile install -DskipTests -Plwjgl-natives-linux-aarch64
xcopy .\varg-lwjgl3\target\original-varg-lwjgl3-0.0.1-SNAPSHOT.jar ..\executable\vargviewer_linuxaarch64_0.3.jar /f /y
