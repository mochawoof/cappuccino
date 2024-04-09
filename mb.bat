:: mochabuilder 1.0.0
@ECHO off
SET JDK=%PROGRAMFILES(x86)%\bluej\jdk
SET MAIN=Main
SET option=UNSET
SET /p option=^> 
IF %option%==b (
    ECHO Cleaning...
    DEL *.class
    ECHO Compiling...
    "%JDK%\bin\javac" *.java
    GOTO end
)
IF %option%==r (
    ECHO Running...
    ECHO.
    "%JDK%\bin\java" %MAIN%
    GOTO end
)
IF %option%==j (
    ECHO Making jar...
    "%JDK%\bin\jar" vcfe %MAIN%.jar %MAIN% *.class
    GOTO end
)
IF %option%==c (
    ECHO Cleaning...
    DEL *.class
    GOTO end
)
ECHO Please specify a valid option!
ECHO b: Build
ECHO r: Run
ECHO j: Make jar
ECHO c: Clean up class files
ECHO.
GOTO end

:end
PAUSE
GOTO :eof