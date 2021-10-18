package covidtracker.model;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import covidtracker.exceptions.DuplicateUserException;
import covidtracker.exceptions.NonExistentUserException;
import org.apache.commons.text.WordUtils;

public final class District {
    private final String name;
    private final Map<Location, HashSet<User>> userLocations;
    private final Map<String, User> users;
    private int numUsers;
    private int numInfected;
    private int numberOfContacted;
    private PrintWriter toPublicPub, toPrivatePub;
    private int publicPubPort;
    private final int size;
    private int publicPortToPub;
    private int privatePubPort;
    private int privatePortToPub;

    public String getName() {
        return name;
    }

    private int getRandomNumber(final int min, final int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public int getPrivatePubPort() {
        return privatePubPort;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public District(
            final String name,
            final int publicPubPort,
            final int publicPortToPub,
            final int privatePubPort,
            final int privatePortToPub)
            throws Exception {
        this.publicPortToPub = publicPortToPub;
        this.privatePubPort = privatePubPort;
        this.privatePortToPub = privatePortToPub;
        this.publicPubPort = publicPubPort;
        this.size = 10;

        ServerSocket ss = new ServerSocket(publicPortToPub);
        new Thread(new Publisher(publicPubPort, publicPortToPub)).start();
        Socket s = ss.accept();
        this.toPublicPub = new PrintWriter(s.getOutputStream(), true);

        ServerSocket ssp = new ServerSocket(privatePortToPub);
        new Thread(new Publisher(privatePubPort, privatePortToPub)).start();
        Socket sp = ssp.accept();
        this.toPrivatePub = new PrintWriter(sp.getOutputStream(), true);

        this.name = name;
        this.users = new HashMap<>();
        this.userLocations = new HashMap<>();
        this.numUsers = 0;
        this.numInfected = 0;
        this.numberOfContacted = 0;
    }

    public int getPublisherPort() {
        return publicPubPort;
    }

    public int getNumUsers() {
        return numUsers;
    }

    public int getNumInfected() {
        return numInfected;
    }

    public int getNumberOfContacted() {
        return numberOfContacted;
    }

    public double getRatio() {
        return (double) numInfected / numUsers;
    }

    public void addUser(final String username) throws DuplicateUserException {
        if (this.users.containsKey(username)) {
            throw new DuplicateUserException();
        }

        User u = new User(username);
        this.numUsers++;
        users.put(username, u);
    }

    public void notifyUserInfection(final String username) throws NonExistentUserException {
        if (!this.users.containsKey(username)) {
            throw new NonExistentUserException();
        }

        var userContacts = this.users.get(username).getContacts();

        for (User x : userContacts) {
            toPrivatePub.println(
                    x.getUsername().toLowerCase()
                            + ":"
                            + x.getUsername()
                            + ", you were in contact with an infected person!");
        }

        if (this.users.get(username).getStatus() != Status.INFECTED) {
            this.numInfected++;
            this.numberOfContacted += userContacts.size();
        }
        this.users.get(username).setStatus(Status.INFECTED);

        toPublicPub.println(
                "sub_infected "
                        + this.name.toLowerCase()
                        + ":There is one more infected in "
                        + this.name
                        + ", it now has "
                        + numInfected
                        + " people infected!");
    }

    public void updateContacts(final Location l) {
        for (User u : userLocations.get(l)) {
            u.getContacts().addAll(userLocations.get(l));
            u.getContacts().remove(u);
        }
    }

    private void addToContact(final User u, final Location l) {
        if (this.userLocations.containsKey(l))
            for (User user : this.userLocations.get(l)) {
                user.getContacts().add(u);
                u.getContacts().add(user);
            }
    }

    public int getPeopleOnLocation(final int x, final int y) {
        Location comp = new Location(x, y);
        HashSet<User> aux = userLocations.get(comp);
        int r;
        if (aux != null) r = aux.size();
        else r = 0;
        return r;
    }

    public boolean isUserInfected(final String username) throws NonExistentUserException {
        if (!this.users.containsKey(username)) {
            throw new NonExistentUserException();
        }

        return this.users.get(username).getStatus() == Status.INFECTED;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void updateUserLocation(final String username, final Location newLocation)
            throws NonExistentUserException {

        if (!this.users.containsKey(username)) {
            throw new NonExistentUserException();
        }

        User u = this.users.get(username);
        if (u.isPlaced()) {
            Location oldLocation = u.getCurrentLocation();
            userLocations.get(oldLocation).remove(u);
            if (userLocations.get(oldLocation).isEmpty()) {
                toPublicPub.println(
                        "sub_empty "
                                + this.name.toLowerCase()
                                + ":"
                                + WordUtils.capitalize(
                                        this.name
                                                .toLowerCase()
                                                .replaceAll("\\s+", " ")
                                                .replaceAll("_", " "))
                                + oldLocation
                                + " does not have people right now!");
            } else if (userLocations.get(oldLocation).size() < 3) {
                toPublicPub.println(
                        "sub_less "
                                + this.name.toLowerCase()
                                + ":"
                                + WordUtils.capitalize(
                                        this.name
                                                .toLowerCase()
                                                .replaceAll("\\s+", " ")
                                                .replaceAll("_", " "))
                                + oldLocation
                                + " is less crowded!");
            }
        } else {
            u.setPlaced(true);
        }

        u.setCurrentLocation(newLocation);
        addToContact(u, newLocation);

        if (!userLocations.containsKey(newLocation)) {
            userLocations.put(newLocation, new HashSet<>());
        }

        userLocations.get(newLocation).add(u);
        if (userLocations.get(newLocation).size() > 10) {
            toPublicPub.println(
                    "sub_more "
                            + this.name.toLowerCase()
                            + ":Location "
                            + newLocation
                            + " is very crowded!");
        }
    }
}
