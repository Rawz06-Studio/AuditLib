# AuditLib — Lightweight Audit Library for Java 25+

A zero-dependency audit library for Java 25+. Annotate your methods with `@Audited` and automatically emit structured audit logs to stdout.

## Features

✅ **Zero Framework Dependencies** — Pure Java, no Spring, no Jakarta, no frameworks  
✅ **Simple Annotations** — `@Audited` on methods or classes  
✅ **Structured Logs** — Parsable format: `AUDIT|class#method|args|status|duration_ms`  
✅ **Parameter Masking** — Regex-based masking for sensitive values  
✅ **Null-Safe** — Audit failures never break your code  
✅ **ByteBuddy Agent** — Runtime instrumentation, zero config needed  

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>fr.rawz06</groupId>
    <artifactId>audit</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. Run with Agent

```bash
java -javaagent:audit-1.0-SNAPSHOT-agent.jar -jar your-app.jar
```

### 3. Annotate Your Methods

```java
import fr.rawz06.audit.annotations.Audited;

public class AuthService {
    
    @Audited(mask = {"password", "token"})
    public void login(String username, String password) {
        // Your code here
    }
}
```

### 4. See Audit Logs

```
AUDIT|com.example.AuthService#login|username=john, password=***|SUCCESS|12
```

## Annotation Reference

### @Audited

```java
@Audited(
    action = "authenticate",      // Optional: custom action name (defaults to method name)
    mask = {"password", "token"}  // Optional: regex patterns to mask parameter names
)
public void login(String username, String password) { ... }
```

**On Class Level:**
```java
@Audited
public class PaymentService {
    public void checkout(String cardNumber, String cvv) { ... }  // auto-audited
    
    @AuditIgnore
    public String getReceiptId() { ... }  // excluded from audit
}
```

### @AuditIgnore

Exclude a single method from class-level `@Audited`:

```java
@Audited
public class UserService {
    public void updateProfile(String userId) { ... }      // audited
    
    @AuditIgnore
    public void getHashedPassword(String userId) { ... }  // NOT audited
}
```

## Masking Patterns

Masking patterns are **regex** matched against parameter names:

```java
@Audited(mask = {
    "password",        // exact match
    "secret.*",        // regex: any param starting with "secret"
    ".*Token",         // regex: any param ending with "Token"
    "card.*Number"     // regex: card...Number patterns
})
public void processPayment(String username, String password, String secretKey, String apiToken, String cardNumber) { ... }
```

Result:
```
AUDIT|com.example.Payment#processPayment|username=john, password=***, secretKey=***, apiToken=***, cardNumber=***|SUCCESS|45
```

## Log Format

For detailed format specification, see [SCHEMA.md](SCHEMA.md).

**Template:**
```
AUDIT|<full.class.name>#<methodName>|<arg1=val1, arg2=val2>|<STATUS>|<duration_ms>
```

- **STATUS**: `SUCCESS` or `ERROR`
- **duration_ms**: milliseconds (or `-1` if calculation failed)
- **args**: `paramName=value` (if compiled with `-parameters`, else `argN=value`)

## Examples

### Simple Method
```java
@Audited
public void transfer(String fromAccount, String toAccount, double amount) {
    // transfer logic
}

// Output:
// AUDIT|com.example.BankService#transfer|fromAccount=ACC001, toAccount=ACC002, amount=5000.0|SUCCESS|156
```

### With Masking
```java
@Audited(mask = {"password", "ssn"})
public void createUser(String email, String password, String ssn) {
    // user creation logic
}

// Output:
// AUDIT|com.example.UserService#createUser|email=user@example.com, password=***, ssn=***|SUCCESS|78
```

### Error Case
```java
@Audited
public void deleteUser(String userId) {
    if (userId == null) {
        throw new IllegalArgumentException("userId required");
    }
}

// If exception thrown:
// AUDIT|com.example.UserService#deleteUser|userId=null|ERROR|3
```

### Class-Level Audit
```java
@Audited
public class TransactionService {
    public void withdraw(String accountId, double amount) { ... }      // audited
    public void deposit(String accountId, double amount) { ... }       // audited
    
    @AuditIgnore
    private String generateTransactionId() { ... }  // NOT audited (private anyway)
}
```

## Compilation Requirements

**Compile with `-parameters` flag** to preserve parameter names:

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

**If not compiled with `-parameters`**, parameters will be named `arg0`, `arg1`, etc.:
```
AUDIT|com.example.Service#login|arg0=john, arg1=***|SUCCESS|8
```

## Robustness

The audit library is designed to **never crash your application**:

- ✅ Audit serialization fails → logged to `System.err`, method still returns
- ✅ Argument `toString()` throws → replaced with `<toString_failed>`
- ✅ Duration calculation fails → logged and duration set to `-1`
- ✅ Internal audit error → `[AUDIT-INTERNAL]` message on stderr, code continues

## Performance

- **Overhead**: < 1% on 10k method calls (benchmark available)
- **Memory**: No accumulation, no thread pools
- **GC Impact**: Minimal string allocation

## Troubleshooting

### No audit logs appear

1. Verify agent JAR is being loaded:
   ```bash
   java -javaagent:audit-1.0-SNAPSHOT-agent.jar -jar your-app.jar 2>&1 | grep -i audit
   ```
   Should see: `[AuditAgent] Installed...`

2. Check that methods are public and have `@Audited`

3. Verify compilation with `-parameters` (optional but recommended)

### Sensitive data is not masked

Ensure mask patterns are **regex** and match parameter names:
```java
@Audited(mask = {"pass.*"})  // matches "password", "passphrase", etc.
public void login(String username, String password) { ... }
```

### Parameter names show as arg0, arg1, ...

Compile with `-parameters` flag. Otherwise, parameter names are unavailable at runtime.

## License

MIT

## Support

For issues or feature requests, open an issue on GitHub.
