cd %~dp0..\..\..\..\..\..\

rem xcopy /Y .\java-solutions\module-info.java .\_build\mymodules\ru.ifmo.rain.klepov.implementor\
xcopy /Y .\java-solutions\ru\ifmo\rain\klepov\implementor\*.java ^
	.\_build\mymodules\ru.ifmo.rain.klepov.implementor\ru\ifmo\rain\klepov\implementor\
echo module ru.ifmo.rain.klepov.implementor {requires info.kgeorgiy.java.advanced.implementor;requires java.compiler;} > .\_build\mymodules\ru.ifmo.rain.klepov.implementor\module-info.java

javac -d .\_build ^
	--module-path ..\java-advanced-2020\artifacts\;..\java-advanced-2020\lib ^
	--module-source-path _build\mymodules\ ^
	-encoding utf8 ^
	--module ru.ifmo.rain.klepov.implementor

echo Class-Path: info.kgeorgiy.java.advanced.implementor.jar > .\_build\manifest.txt
jar --create ^
	--manifest .\_build\manifest.txt ^
	--file .\_implementor.jar ^
	--main-class ru.ifmo.rain.klepov.implementor.Implementor ^
	-C .\_build\ru.ifmo.rain.klepov.implementor .
del .\_build\manifest.txt
