# QUICKSTART.md — 5 Minutes to Audit Logs

## 1. Add the Dependency

Maven:
```xml
<dependency>
    <groupId>fr.rawz06</groupId>
    <artifactId>audit</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Gradle:
```gradle
implementation 'fr.rawz06:audit:1.0-SNAPSHOT'
```

## 2. Annotate a Method

```java
import fr.rawz06.audit.annotations.Audited;
import org.springframework.stereotype.Service;

@Service
public class MyService {
    
    @Audited
    public void doSomething(String userId, String action) {
        System.out.println("Processing " + action + " for user " + userId);
    }
}
```

## 3. Run Your Spring Boot App

```bash
mvn spring-boot:run
```

## 4. Call the Method

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
