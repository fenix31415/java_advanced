cd %~dp0..\..\..\..\..\..\

set S=..\java-advanced-2020\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
javadoc -d _javadoc ^
    -link https://docs.oracle.com/en/java/javase/11/docs/api/ ^
    -private ^
    -encoding utf8 ^
    -docencoding utf8 ^
    --module-path ..\java-advanced-2020\lib ^
    --module-source-path .\_build\mymodules\;..\java-advanced-2020\modules ^
    --module ru.ifmo.rain.klepov.implementor ^
    %S%\Impler.java ^
    %S%\JarImpler.java ^
    %S%\ImplerException.java
