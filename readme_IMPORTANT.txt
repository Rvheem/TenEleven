1. ADD OPENCV 4.9 AND 4.7 to your external libraries
2. CHANGE THE DIRECTORIES IN THE CLASS FACERECOG METHODS PREDICT AND EMBED TO THE path of:
   your dir\face-recognition-java\cli\target\face-recognition-cli-0.3.1.jar
   your dir\face-recognition-java\embeddings.dat
   String resultFilePath = "your dir\\result.txt";
3. CHANGE THE PATH IN ADDEMP TO THE PATH OF YOUR EMPLOYEE DATASET
 employeeDirectoryPath = "C:/PROJECTS/teneleven/employees/"
 embedFace("C:/PROJECTS/teneleven/employees");
4. IN DASH
change the cascade classifier to the dir in your computer ( in the dependencies folder )
:CascadeClassifier("D:\\TenEleven\\TenEleven\\employees\\haarcascade_frontalface_default.xml")
change : String imagePath = "C:\\PROJECTS\\cam.jpg";
to : yourdir\\cam.jpg

6.ADD JDK_HOME bin and OPENCV bin TO YOUR ENVIRONEMENT VARIABLES path VARIABLE
7.MAKE SURE YOU ALLOW TEN ELEVEN IN YOUR BACKGROUND EXCEPTIONS