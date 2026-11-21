package com.medisync.app.model;

/** Common base for anything with an identity and a display name (inheritance root for User and Patient). */
public abstract class Person {
    private final long id;
    private String name;
    private String avatar;

    protected Person(long id, String name, String avatar) {
        this.id = id;
        setName(name);
        setAvatar(avatar);
    }

    public long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        this.name = name.trim();
    }

    public String getAvatar() { return avatar == null ? "" : avatar; }

    public void setAvatar(String avatar) { this.avatar = avatar == null ? "" : avatar; }

    /** Short label shown under the name on screens. */
    public abstract String getDisplayRole();
}
