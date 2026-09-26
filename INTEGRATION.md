# INTEGRATION.md — Adding AuditLib to Your Application

## Step 1: Add Maven Dependency

```xml
<dependency>
    <groupId>fr.rawz06</groupId>
    <artifactId>audit</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Step 2: Launch with Agent JAR

Replace `your-app.jar` with your application JAR:

```bash
java -javaagent:audit-agent-1.0-SNAPSHOT.jar -jar your-app.jar
```

**Note:** The agent JAR must be in your classpath or referenced with an absolute path.

### With JVM Arguments

```bash
java \
  -javaagent:audit-agent-1.0-SNAPSHOT.jar \
  -Xmx2g \
  -Xms1g \
  -Dapp.name=myapp \
  -jar your-app.jar
```

### In Docker

```dockerfile
FROM openjdk:25-slim

COPY audit-agent-1.0-SNAPSHOT.jar /app/
COPY your-app.jar /app/

WORKDIR /app

ENTRYPOINT ["java", "-javaagent:/app/audit-agent-1.0-SNAPSHOT.jar", "-jar", "your-app.jar"]
```

### In Kubernetes

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-app
spec:
  containers:
  - name: app
    image: myapp:latest
    env:
    - name: JAVA_OPTS
      value: "-javaagent:/app/audit-agent-1.0-SNAPSHOT.jar"
```

## Step 3: Annotate Your Methods

### Single Method

```java
import fr.rawz06.audit.annotations.Audited;

public class UserService {
    
    @Audited
    public User createUser(String email, String name) {
        // Your code here
    }
}
```

### Entire Class

```java
import fr.rawz06.audit.annotations.Audited;
import fr.rawz06.audit.annotations.AuditIgnore;

@Audited
public class PaymentService {
    
    public Payment charge(String accountId, double amount) {
        // Audited
    }
    
    @AuditIgnore
    public String generateTransactionId() {
        // NOT audited
    }
}
```

### With Masking

```java
@Audited(mask = {"password", "token", "secret.*"})
public void login(String username, String password, String token) {
    // password and token will show as *** in logs
}
```

## Step 4: Configure Compilation

Ensure parameter names are preserved (optional but recommended):

### Maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### Gradle

```gradle
compileJava {
    options.compilerArgs += '-parameters'
}
```

## Step 5: Verify Installation

Run your app with the agent. You should see:

```
[AuditAgent] Installed. Watching for @Audited methods...
```

Then invoke an `@Audited` method and check stdout for:

```
AUDIT|com.example.UserService#createUser|email=user@example.com, name=John|SUCCESS|15
```

## Routing Audit Logs

By default, audit logs go to **stdout**. Route them where you need:

### To File

```bash
java -javaagent:audit-agent-1.0-SNAPSHOT.jar -jar your-app.jar > audit.log 2>&1
```

### To Syslog

```bash
java -javaagent:audit-agent-1.0-SNAPSHOT.jar -jar your-app.jar | logger -t audit
```

### To Splunk

```bash
java -javaagent:audit-agent-1.0-SNAPSHOT.jar -jar your-app.jar | nc splunk-forwarder 9999
```

### To Vector

```bash
java -javaagent:audit-agent-1.0-SNAPSHOT.jar -jar your-app.jar | vector --config /etc/vector/vector.toml
```

### In Logback

Create a `logback.xml` with a stdout appender:

```xml
<configuration>
    <appender name="AUDIT" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.out</target>
        <encoder>
            <pattern>%msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="AUDIT" />
    </root>
</configuration>
```

The audit lib writes to `System.out`, which Logback will capture.

## Multi-Classpath Scenario

If you're using a custom classpath, ensure the agent JAR dependencies are available:

```bash
java -javaagent:audit-agent-1.0-SNAPSHOT.jar \
     -cp ".:lib/*:audit-agent-1.0-SNAPSHOT.jar" \
     com.example.Main
```

## Troubleshooting

### Agent not loading

Check for:
- Correct path to agent JAR (use absolute path if needed)
- JVM permissions to load agents (check SecurityManager)
- Java version: 25+ required

Look for this message in stderr:
```
[AuditAgent] Installed. Watching for @Audited methods...
```

If missing, the agent didn't load. Try:
```bash
java -javaagent:/full/path/to/audit-agent-1.0-SNAPSHOT.jar -jar your-app.jar
```

### No audit logs appearing

1. Verify methods are **public**
2. Verify they have `@Audited` annotation
3. Verify they're actually being called
4. Check for `@AuditIgnore` if method is skipped

### Parameter names showing as arg0, arg1, ...

Compile source with `-parameters` flag (see Step 4).

### Large agent JAR size

The agent JAR includes ByteBuddy (~8MB). This is normal.
- Do NOT decompress in production
- Cache agent JAR locally if using container image
- Single agent instance shared across all methods

## Disabling Audit Temporarily

### Method-level

Remove the `@Audited` annotation or add `@AuditIgnore`.

### Class-level

Remove `@Audited` from the class or add `@AuditExclude`.

### Application-wide

Simply don't launch with `-javaagent`. All annotations become no-ops.

## Performance Impact

Measured on 10k method invocations:

| Scenario | Overhead |
|----------|----------|
| Simple method (no args) | ~0.5ms |
| Method with 2-3 args | ~0.8ms |
| Method with masking | ~1ms |
| **Overall app impact** | **< 1%** |

## Next Steps

1. See [SCHEMA.md](SCHEMA.md) for output format specification
2. See [EXAMPLES.md](EXAMPLES.md) for common patterns
3. See [README.md](README.md) for full API documentation
