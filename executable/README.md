# VARG viewer Java executable (jar file)  
  
VARG is a Java project, to help running the viewer without the need to build the project yourself I have added a .jar  
To run you need to have a Java Runtime Environment (JRE) installed.  
  
To install go to https://www.java.com/en/download/windows_manual.jsp  
Or just google 'install java'  
  
When you have JRE installed - you can verify by opening a command line prompt and type:  
>java -version  
  
If installed properly it will notify you of the version.  
  
## Running the VARG viewer  
  
Currently the viewer is pre-loaded with 4 very simple glbs.  
Use the left/right arrow keys to switch model.  
Working on an option to specify local folder for glb/gltf files.  
  
  
In your command line prompt, navigate to the 'executable' folder and run the jar file for your platform by starting a command line shell.  
Navigate to the 'executable' folder (or where you have the .jar file) and execute the command for your platform:   
    
### For Windows:  
  
->java -jar vargviewer_winx64_0.3.jar  
  
### For Mac  
  
->java -jar vargviewer_macx64_0.3.jar  
  
### For Linux  
  
Intel/Amd architecture:  
->java -jar vargviewer_linuxx64_0.3.jar  
  
Arm 64 bit  
->java -jar vargviewer_linuxaarch64_0.3.jar  
  

Please report crashes by opening an issue and attach the output from the command prompt, please include all of the text not just the crash exception.  
  
  

