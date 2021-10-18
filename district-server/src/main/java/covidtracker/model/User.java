package covidtracker.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class User {

    private final String username;
    private Location currentLocation;
    private Status status;
    private boolean placed;
    private final Set<User> contacts;

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(final boolean placed) {
        this.placed = placed;
    }

    public User(final String user) {
        this.username = user;
        this.status = Status.HEALTHY;
        this.placed = false;
        this.contacts = new HashSet<>();
        this.currentLocation = null;
    }

    public String getUsername() {
        return this.username;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(final Status newStatus) {
        this.status = newStatus;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public Set<User> getContacts() {
        return contacts;
    }

    @Override
    public boolean equals(final Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }
        /* Check if o is an instance of Complex or not
        "null instanceof [type]" also returns false */
        if (!(o instanceof User)) {
            return false;
        }
        return ((User) o).getUsername().equals(this.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getCurrentLocation(), getStatus());
    }

    public void setCurrentLocation(final Location currentLocation) {
        this.currentLocation = currentLocation;
        placed = true;
    }
}
