# 12 - 启动命令清单

## 1. 执行位置
- 先进入项目根目录：`D:\xiaoji\xiaoji`
- 下面命令默认在 PowerShell 执行。

## 2. Maven 配置（本项目已硬绑定）
- 本项目默认使用：
  - `D:/maven/apache-maven-3.8.3/conf/settings-xiaoji.xml`
- 本地仓库：
  - `D:\maven\repo-xiaoji`

## 3. JDK 切换命令
- 使用 Java 8：
  - `.\mvn8.cmd -v`
  - `.\mvnx.cmd 8 -v`
- 使用 Java 17：
  - `.\mvn17.cmd -v`
  - `.\mvnx.cmd 17 -v`

## 4. 常用构建命令
- Java 8 编译（不跑测试）：
  - `.\mvnx.cmd 8 clean compile -DskipTests`
- Java 17 编译（不跑测试）：
  - `.\mvnx.cmd 17 clean compile -DskipTests`
- 全量校验（不编译测试代码）：
  - `.\mvnx.cmd 8 validate`

## 5. 后端启动命令
- 启动主程序模块：
  - `.\mvnx.cmd 8 -pl backend/app-bootstrap spring-boot:run`
- 指定 profile 启动（示例 dev）：
  - `.\mvnx.cmd 8 -pl backend/app-bootstrap spring-boot:run -Dspring-boot.run.profiles=dev`

## 6. 环境变量（可选）
- 如果 JDK 路径不是脚本默认值，可在当前终端设置：
  - Java 8：`$env:XIAOJI_JAVA8_HOME="C:\Program Files\Java\jdk1.8.0_161"`
  - Java 17：`$env:XIAOJI_JAVA17_HOME="D:\Java\jdk-17"`

## 7. 快速排查
- 看当前 Maven/JDK 版本：
  - `.\mvnx.cmd 8 -v`
  - `.\mvnx.cmd 17 -v`
- 检查项目绑定 settings：
  - `Get-Content .\.mvn\maven.config -Raw`
