package fr.rawz06.audit.example;

public class ExampleApp {

    public static void main(String[] args) {
        AuthService auth = new AuthService();

        System.out.println("\n=== Test 1: Simple login (audited at class level) ===");
        auth.login("alice", "secret123");

        System.out.println("\n=== Test 2: Authenticate with masking ===");
        String sessionId = auth.authenticate("bob", "mypassword", "token-xyz-789");
        System.out.println("Session ID: " + sessionId);

        System.out.println("\n=== Test 3: hashPassword (should NOT be audited - @AuditIgnore) ===");
        String hashed = auth.hashPassword("secret");
        System.out.println("Hashed: " + hashed);

        System.out.println("\n=== Test 4: Error case ===");
        try {
            auth.login(null, null);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());
        }

        System.out.println("\nDone!");
    }
}
