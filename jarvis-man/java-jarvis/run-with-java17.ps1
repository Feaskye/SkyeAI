# 设置Java 17环境变量
$JAVA_HOME = "D:\Program Files\JetBrains\javajdks\temurin-17.0.17"
$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;$env:PATH"

# 验证Java版本
echo "使用Java版本:"
java -version

# 验证Maven版本
echo "使用Maven版本:"
mvn -version

# 执行Maven命令
if ($args.Count -gt 0) {
    $command = $args -join " "
    echo "执行Maven命令: $command"
    mvn $command
} else {
    echo "请指定Maven命令，例如: ./run-with-java17.ps1 clean install"
}