# Gradle → Maven 변환 가이드

## 📁 1. 디렉토리 구조 변경

### 현재 Gradle 구조
```
simple-llm-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradlew, gradlew.bat
```

### Maven 구조로 변경
```
simple-llm-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/          # 추가 필요
├── pom.xml                     # 새로 생성
├── .mvn/                       # Maven Wrapper (선택사항)
└── mvnw, mvnw.cmd             # Maven Wrapper 스크립트
```

## 📄 2. pom.xml 생성

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Spring Boot Parent -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.12.RELEASE</version>
        <relativePath/>
    </parent>
    
    <!-- Project Information -->
    <groupId>com.example</groupId>
    <artifactId>simple-llm-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <name>Simple LLM Backend</name>
    <description>LLM 추론 테스트 백엔드 - Windows 10 Pro 환경용</description>
    
    <!-- Properties -->
    <properties>
        <java.version>11</java.version>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        
        <!-- Dependency Versions -->
        <lombok.version>1.18.20</lombok.version>
        <jackson.version>2.11.4</jackson.version>
    </properties>
    
    <!-- Dependencies -->
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Oracle JDBC -->
        <dependency>
            <groupId>com.oracle.ojdbc</groupId>
            <artifactId>ojdbc7</artifactId>
            <version>12.1.0.2</version>
        </dependency>
        
        <!-- JSON Processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>
        
        <!-- Configuration Processor -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- H2 Database (Runtime) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <!-- Build Configuration -->
    <build>
        <finalName>${project.artifactId}-${project.version}</finalName>
        
        <plugins>
            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                    <encoding>UTF-8</encoding>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-configuration-processor</artifactId>
                            <version>2.3.12.RELEASE</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            
            <!-- Maven Surefire Plugin (테스트) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/*Tests.java</include>
                    </includes>
                </configuration>
            </plugin>
            
            <!-- JaCoCo Code Coverage -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.6</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            
            <!-- Maven Resources Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
    <!-- Profiles -->
    <profiles>
        <!-- Development Profile -->
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <spring.profiles.active>dev</spring.profiles.active>
            </properties>
        </profile>
        
        <!-- Production Profile -->
        <profile>
            <id>prod</id>
            <properties>
                <spring.profiles.active>prod</spring.profiles.active>
            </properties>
        </profile>
        
        <!-- Integration Test Profile -->
        <profile>
            <id>integration-test</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-failsafe-plugin</artifactId>
                        <version>2.22.2</version>
                        <executions>
                            <execution>
                                <goals>
                                    <goal>integration-test</goal>
                                    <goal>verify</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
    
    <!-- Repositories (필요시) -->
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
    
    <pluginRepositories>
        <pluginRepository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

## 🔧 3. Maven Wrapper 설정

### Maven Wrapper 생성
```bash
# Maven이 설치되어 있다면
mvn -N io.takari:maven:wrapper -Dmaven=3.8.6

# 또는 직접 파일 생성
```

### .mvn/wrapper/maven-wrapper.properties
```properties
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar
```

## 📝 4. 스크립트 파일 생성

### scripts/run-maven.bat
```batch
@echo off
echo ========================================
echo  Simple LLM Backend Starting (Maven)...
echo ========================================

REM 환경변수 파일 로드
if exist ".env" (
    echo Loading environment variables from .env file...
    for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%b"=="" (
            set "%%a=%%b"
        )
    )
) else (
    echo .env file not found, using default values...
)

REM Maven 옵션 설정
set "MAVEN_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Djava.awt.headless=true"

REM Spring 프로필 기본값 설정
if not defined SPRING_PROFILES_ACTIVE (
    set "SPRING_PROFILES_ACTIVE=dev"
)

echo Maven Options: %MAVEN_OPTS%
echo Spring Profile: %SPRING_PROFILES_ACTIVE%
echo vLLM URL: %VLLM_BASE_URL%
echo SGLang URL: %SGLANG_BASE_URL%
echo.

REM JAR 파일 경로 확인
set "JAR_FILE=target\simple-llm-backend-0.0.1-SNAPSHOT.jar"
if not exist "%JAR_FILE%" (
    echo JAR file not found, building project...
    call mvnw.cmd clean package -DskipTests
    if %errorLevel% neq 0 (
        echo Build failed!
        pause
        exit /b 1
    )
)

REM 로그 디렉토리 생성
if not exist "logs" mkdir logs

echo Starting application...
echo.
java %MAVEN_OPTS% -jar "%JAR_FILE%" --spring.profiles.active=%SPRING_PROFILES_ACTIVE%

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application failed to start. Check logs for details.
    pause
)
```

### scripts/test-maven.bat
```batch
@echo off
echo ===============================
echo  Running Tests with Maven...
echo ===============================

REM 단위 테스트 실행
echo Running unit tests...
call mvnw.cmd test

if %errorLevel% equ 0 (
    echo.
    echo ===============================
    echo  Unit Tests: PASSED
    echo ===============================
    echo.
    
    REM 통합 테스트 실행
    echo Running integration tests...
    call mvnw.cmd verify -Pintegration-test
    
    if %errorLevel% equ 0 (
        echo.
        echo ===============================
        echo  All Tests: PASSED
        echo ===============================
        
        REM 테스트 리포트 열기
        if exist "target\site\jacoco\index.html" (
            echo Opening coverage report...
            start target\site\jacoco\index.html
        )
    ) else (
        echo.
        echo ===============================
        echo  Integration Tests: FAILED
        echo ===============================
    )
) else (
    echo.
    echo ===============================
    echo  Unit Tests: FAILED
    echo ===============================
)

pause
```

## 🚀 5. Maven 명령어 가이드

### 기본 빌드 명령어
```bash
# 프로젝트 컴파일
./mvnw compile

# 테스트 실행
./mvnw test

# 패키지 생성 (JAR)
./mvnw package

# 의존성 다운로드 및 전체 빌드
./mvnw clean install

# 테스트 스킵하고 빌드
./mvnw clean package -DskipTests

# 애플리케이션 실행
./mvnw spring-boot:run

# 특정 프로필로 실행
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 개발 관련 명령어
```bash
# 의존성 트리 확인
./mvnw dependency:tree

# 의존성 분석
./mvnw dependency:analyze

# 코드 커버리지 리포트 생성
./mvnw test jacoco:report

# 통합 테스트 실행
./mvnw verify -Pintegration-test

# 라이브 리로드로 실행
./mvnw spring-boot:run -Dspring-boot.run.fork=false
```

## 📊 6. Gradle vs Maven 명령어 비교

| 작업 | Gradle | Maven |
|------|--------|-------|
| 빌드 | `./gradlew build` | `./mvnw package` |
| 테스트 | `./gradlew test` | `./mvnw test` |
| 실행 | `./gradlew bootRun` | `./mvnw spring-boot:run` |
| 정리 | `./gradlew clean` | `./mvnw clean` |
| 의존성 확인 | `./gradlew dependencies` | `./mvnw dependency:tree` |
| JAR 생성 | `./gradlew bootJar` | `./mvnw package` |

## 🔄 7. 변환 체크리스트

### 파일 생성/수정
- [ ] `pom.xml` 생성
- [ ] Maven Wrapper 파일 생성 (.mvn/, mvnw, mvnw.cmd)
- [ ] `scripts/run-maven.bat` 생성
- [ ] `scripts/test-maven.bat` 생성

### 파일 제거 (선택사항)
- [ ] `build.gradle` 삭제 또는 백업
- [ ] `settings.gradle` 삭제 또는 백업  
- [ ] `gradle.properties` 삭제 또는 백업
- [ ] `gradlew`, `gradlew.bat` 삭제
- [ ] `gradle/` 디렉토리 삭제

### 테스트
- [ ] `./mvnw clean compile` 실행 확인
- [ ] `./mvnw test` 실행 확인
- [ ] `./mvnw package` 실행 확인
- [ ] `./mvnw spring-boot:run` 실행 확인
- [ ] JAR 파일 직접 실행 확인

## 💡 8. 추가 팁

### IDE 설정
- **IntelliJ IDEA**: File → Project Structure → Modules에서 Maven 프로젝트로 인식되는지 확인
- **Eclipse**: Maven 프로젝트로 Import
- **VS Code**: Java Extension Pack에서 Maven 지원 확인

### 성능 최적화
```xml
<!-- pom.xml에 추가 -->
<properties>
    <maven.compiler.useIncrementalCompilation>false</maven.compiler.useIncrementalCompilation>
    <maven.test.skip>false</maven.test.skip>
</properties>
```

### Windows 환경 최적화
```batch
REM .mvn/jvm.config 파일 생성
-Xms512m -Xmx2g -XX:+UseG1GC -Dfile.encoding=UTF-8
```
