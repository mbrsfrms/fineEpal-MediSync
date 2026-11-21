package com.medisync.app.model;

/** A person whose medications are managed: the account owner, a child, or an elderly dependent. */
public class Patient extends Person {
    public static final String CAT_CHILD = "Child";
    public static final String CAT_ELDERLY = "Elderly";
    public static final String CAT_ADULT = "Adult";

    private final long ownerId;
    private String category;
    private String relationship;
    private String dateOfBirth;
    private String allergies;

    public Patient(long id, long ownerId, String name, String category, String relationship,
                   String dateOfBirth, String avatar, String allergies) {
        super(id, name, avatar);
        this.ownerId = ownerId;
        this.category = category == null || category.isEmpty() ? CAT_ADULT : category;
        this.relationship = relationship == null ? "" : relationship;
        this.dateOfBirth = dateOfBirth == null ? "" : dateOfBirth;
        this.allergies = allergies == null ? "" : allergies.trim();
    }

    public long getOwnerId() { return ownerId; }

    public String getCategory() { return category; }

    public String getRelationship() { return relationship; }

    public void setRelationship(String relationship) { this.relationship = relationship == null ? "" : relationship; }

    public String getDateOfBirth() { return dateOfBirth; }

    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth == null ? "" : dateOfBirth; }

    /** Free-text allergies recorded for this patient, e.g. "Penicillin, peanuts". Empty when none. */
    public String getAllergies() { return allergies; }

    public void setAllergies(String allergies) { this.allergies = allergies == null ? "" : allergies.trim(); }

    public boolean hasAllergies() { return !allergies.isEmpty(); }

    @Override
    public String getDisplayRole() {
        return category + (relationship.isEmpty() ? "" : " · " + relationship);
    }
}
