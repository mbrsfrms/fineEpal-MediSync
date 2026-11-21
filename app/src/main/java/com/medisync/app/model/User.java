package com.medisync.app.model;

/** A registered MediSync account. The password hash is never held by this model. */
public class User extends Person {
    private final String email;

    public User(long id, String name, String email, String avatar) {
        super(id, name, avatar);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        this.email = email.trim().toLowerCase(java.util.Locale.US);
    }

    public String getEmail() { return email; }

    @Override
    public String getDisplayRole() { return "Account holder"; }
}
