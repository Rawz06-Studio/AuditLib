package fr.rawz06.audit.example;

import fr.rawz06.audit.annotations.Audited;
import fr.rawz06.audit.annotations.AuditIgnore;

@Audited
public class AuthService {

    public void login(String username, String password) {
        System.out.println("Logging in user: " + username);
    }

    @Audited(mask = {"password", "token"})
    public String authenticate(String username, String password, String token) {
        System.out.println("Authenticating with token");
        return "session-" + System.nanoTime();
    }

    @AuditIgnore
    public String hashPassword(String password) {
        return "hashed-" + password.hashCode();
    }
}
