# QUICKSTART.md — 5 Minutes to Audit Logs

## 1. Get the JARs

You need two JARs from the `target/` directory:
- `audit-1.0-SNAPSHOT.jar` — The library (18 KB)
- `audit-agent-1.0-SNAPSHOT.jar` — The ByteBuddy agent (8 MB)

## 2. Add Dependency to Your Project

### Maven
```xml
<dependency>
    <groupId>fr.rawz06</groupId>
    <artifactId>audit</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Gradle
```gradle
dependencies {
    implementation 'fr.rawz06:audit:1.0-SNAPSHOT'
}
```

## 3. Annotate a Method

```java
import fr.rawz06.audit.annotations.Audited;

public class MyService {
    
    @Audited
    public void doSomething(String userId, String action) {
        System.out.println("Processing " + action + " for user " + userId);
    }
}
```

## 4. Run Your App with the Agent

```bash
java -javaagent:audit-agent-1.0-SNAPSHOT.jar -jar your-app.jar
```

## 5. See the Audit Log

When you call `doSomething("user123", "delete")`, you'll see:

```
AUDIT|com.example.MyService#doSomething|userId=user123, action=delete|SUCCESS|5
```

---

## Add Masking (Bonus)

Mask sensitive values:

```java
@Audited(mask = {"password", "token"})
public void login(String username, String password, String token) { ... }
```

Output:
```
AUDIT|com.example.Auth#login|username=john, password=***, token=***|SUCCESS|8
```

---

## That's It!

For more details:
- [README.md](README.md) — Full documentation
- [SCHEMA.md](SCHEMA.md) — Output format specification
- [EXAMPLES.md](EXAMPLES.md) — Common patterns
- [INTEGRATION.md](INTEGRATION.md) — Advanced setup
