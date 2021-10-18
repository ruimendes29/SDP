package covidtracker.model;

public final class Location implements Comparable<Location> {

    private final int x;
    private final int y;

    public Location(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public boolean equals(final Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }
        /* Check if o is an instance of Complex or not
        "null instanceof [type]" also returns false */
        if (!(o instanceof Location)) {
            return false;
        }
        // typecast o to Complex so that we can compare data members
        Location other = (Location) o;
        return (other.x == this.x && other.y == this.y);
    }

    @Override
    public int hashCode() {
        String ret = this.x + "0" + this.y;
        return Integer.parseInt(ret);
    }

    @Override
    public String toString() {
        return "{" + "x=" + x + ", y=" + y + '}';
    }

    public int compareTo(final Location l) {
        return l.getX() - this.x + l.getY() - this.y;
    }
}
