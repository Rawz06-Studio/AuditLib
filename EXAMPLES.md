# EXAMPLES.md — Usage Patterns for AuditLib

## Common Masking Patterns

### Authentication & Security

```java
@Audited(mask = {"password", "pin", "secret", "token"})
public void login(String username, String password) { ... }

@Audited(mask = {"bearerToken", "apiKey", "clientSecret"})
public void authenticate(String clientId, String bearerToken, String apiKey) { ... }

@Audited(mask = {"ssn", "creditCard", "cvv"})
public void verifyIdentity(String ssn, String creditCard, String cvv) { ... }
```

### Regex Patterns

```java
// Match anything containing "secret" (case-sensitive)
@Audited(mask = {".*[Ss]ecret.*"})
public void process(String appSecret, String userSecret, String clientSecret) { ... }

// Match all token-like parameters
@Audited(mask = {".*[Tt]oken.*", ".*[Kk]ey.*"})
public void sendRequest(String bearerToken, String apiToken, String encryptionKey) { ... }

// Match numbered secrets (secret1, secret2, ...)
@Audited(mask = {"secret\\d+"})
public void storeSecrets(String secret1, String secret2, String secret3) { ... }
```

## Class-Level vs Method-Level

### Audit entire class (public methods only)

```java
@Audited
public class PaymentService {
    public void charge(String cardNumber, double amount) { ... }
    public void refund(String transactionId, double amount) { ... }
    
    @AuditIgnore
    public boolean validateCard(String cardNumber) { ... }  // Excluded
    
    private void logInternalTransaction(String txId) { ... }  // Never audited (private)
}
```

### Fine-grained control on methods

```java
@Audited(mask = {".*[Pp]assword.*"})  // Class-level rule
public class AuthService {
    public void login(String username, String password) { ... }  // Audited + masked
    
    @Audited(mask = {"mfaSecret"})    // Method-level override
    public void setupMFA(String mfaSecret) { ... }  // Uses method-level mask
    
    @AuditIgnore
    public String generateHash(String data) { ... }  // Not audited
}
```

## Real-World Examples

### E-commerce: Order Processing

```java
@Audited(mask = {"cvv", "cardNumber", ".*[Pp]assword.*"})
public class OrderService {
    public Order createOrder(String customerId, OrderDetails details, String cvv) { ... }
    
    @Audited
    public void shipOrder(String orderId) { ... }
    
    @Audited(mask = {".*[Tt]ransaction.*"})
    public Payment processPayment(String orderId, String cardNumber, String transactionId) { ... }
}
```

### SaaS: User Management

```java
@Audited(mask = {"password", "secret.*", "token"})
public class UserService {
    public User createUser(String email, String password) { ... }
    
    @Audited(mask = {"oldPassword", "newPassword"})
    public void changePassword(String userId, String oldPassword, String newPassword) { ... }
    
    @Audited
    public void activateUser(String userId) { ... }
    
    @AuditIgnore
    private String hashPassword(String password) { ... }
}
```

### API Gateway

```java
@Audited(mask = {"authorization", "x-api-key", ".*[Tt]oken.*"})
public class ApiGateway {
    public Response handleRequest(String path, String method, String authorization, String body) { ... }
    
    @Audited(mask = {"apiKey"})
    public void registerClient(String clientName, String apiKey) { ... }
}
```

## Querying Audit Logs

### With Vector / VictoriaLogs

```
// Find all failed authentications
query: 'AUDIT|.*#authenticate|.*|ERROR|'

// Find all successful logins in last hour
query: 'AUDIT|.*#login|.*|SUCCESS|'
filter: 'timestamp > now() - 1h'

// Find masking in action (failed password attempts)
query: 'AUDIT|.*#login|.*\*\*\*.*|'

// Performance analysis: methods taking > 100ms
query: 'AUDIT|.*|.*|SUCCESS|(?<duration>[0-9]{3,})'
filter: 'duration > 100'
```

### Elastic / Kibana

```json
{
  "query": {
    "bool": {
      "must": [
        { "match": { "message": "AUDIT" } },
        { "match": { "method": "login" } },
        { "match": { "status": "ERROR" } }
      ],
      "filter": [
        { "range": { "timestamp": { "gte": "now-24h" } } }
      ]
    }
  }
}
```

### Splunk

```
index=audit source::AUDIT method=authenticate status=ERROR
| stats count by username
| sort - count
```

## Performance Considerations

### Audit overhead per call (typical)

| Method Type | Args | Overhead |
|------------|------|----------|
| 0 args | - | ~0.5ms |
| 2-3 simple args (String, int) | - | ~0.8ms |
| Large object arg | toString() < 100 chars | ~1ms |
| Large object arg | toString() > 2000 chars | ~1.5ms (truncated) |

### Best practices for performance

```java
// ✅ GOOD: Audit sparingly on high-traffic paths
public class ApiService {
    @Audited(mask = {"token"})
    public Response handleApiRequest(String token, Request req) { ... }  // 1 audit per request
}

// ❌ AVOID: Auditing in tight loops
for (int i = 0; i < 1_000_000; i++) {
    service.processItem(item);  // Don't audit this in hot loop
}

// ✅ GOOD: Batch audit logging
@Audited
public void processBatch(List<Item> items) { ... }  // 1 audit for entire batch
```

## Troubleshooting Common Patterns

### "I see arg0, arg1, ... instead of parameter names"

**Problem:** Compiled without `-parameters` flag.

**Solution:**
```xml
<!-- Maven -->
<compilerArgs>
    <arg>-parameters</arg>
</compilerArgs>
```

```gradle
// Gradle
tasks.named('compileJava') {
    options.compilerArgs += '-parameters'
}
```

### "Sensitive value not masked"

**Problem:** Pattern doesn't match parameter name or regex is wrong.

```java
// ❌ WRONG: Pattern doesn't match
@Audited(mask = {"pwd"})
public void login(String username, String password) { ... }
// Output: AUDIT|...|username=alice, password=secret123|...

// ✅ CORRECT: Match parameter name
@Audited(mask = {"password"})
public void login(String username, String password) { ... }
// Output: AUDIT|...|username=alice, password=***|...

// ✅ CORRECT: Use regex
@Audited(mask = {"pass.*", ".*word"})
public void login(String username, String password) { ... }
```

### "Method is not audited even though class is @Audited"

**Reasons:**
- Method is `private` or `package-private` (only public methods audited)
- Method has `@AuditIgnore` annotation
- Class has `@AuditExclude` annotation

**Solution:** Verify visibility and annotations.

## Filtering & Bookmarking in VictoriaLogs UI

### Bookmark: Production Errors

```
AUDIT|.*|.*|ERROR| > timestamp=last_7d
```

### Bookmark: High-latency calls

```
match: "AUDIT" | stats: duration > 1000ms | top 10
```

### Bookmark: Sensitive data access

```
match: "AUDIT" AND (password=*** OR token=*** OR cvv=***)
```
