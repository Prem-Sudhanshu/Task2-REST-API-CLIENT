@echo off
echo Compiling WeatherClient.java...
javac -cp ".;lib/json-20240303.jar" WeatherClient.java
if errorlevel 1 (
    echo Compilation failed!
    pause
) else (
    echo Compilation successful!
    echo Running the program...
    java -cp ".;lib/json-20240303.jar" WeatherClient
) 
