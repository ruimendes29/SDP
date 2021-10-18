package covidtracker.client;

import covidtracker.api.District;
import covidtracker.api.Location;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

public final class HttpClient {
    private final Client client;
    private final URI api;

    public static final class Builder {
        private String protocol =
                Optional.ofNullable(System.getenv("APP_SERVER_SCHEME")).orElse("http");
        private String hostname =
                Optional.ofNullable(System.getenv("APP_SERVER_HOSTNAME")).orElse("127.0.0.1");
        private int port =
                Integer.parseInt(
                        Optional.ofNullable(System.getenv("APP_SERVER_PORT")).orElse("9090"));

        public Builder protocol(final String scheme) {
            this.protocol = scheme;
            return this;
        }

        public Builder hostname(final String host) {
            this.hostname = host;
            return this;
        }

        public Builder port(final int gate) {
            this.port = gate;
            return this;
        }

        public HttpClient build() {
            return new HttpClient(this);
        }
    }

    private HttpClient(final Builder builder) {
        this.client = ClientBuilder.newClient(new ClientConfig());
        this.api =
                UriBuilder.fromUri("")
                        .scheme(builder.protocol)
                        .host(builder.hostname)
                        .port(builder.port)
                        .path("api")
                        .path("v1")
                        .build();
    }

    public List<District> getDistricts() {
        var endpoint = UriBuilder.fromUri(this.api).path("districts").build();

        return this.client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<>() {});
    }

    public District getDistrict(final String name) {
        var district = name.toLowerCase().strip().replaceAll("\\s+", "_");
        var endpoint = UriBuilder.fromUri(this.api).path("districts").path(district).build();

        return this.client.target(endpoint).request(MediaType.APPLICATION_JSON).get(District.class);
    }

    public District createDistrict(final District district) {
        var endpoint = UriBuilder.fromUri(this.api).path("districts").build();

        return this.client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(district))
                .readEntity(District.class);
    }

    public District updateDistrict(final District district) {
        var name = district.getName().strip().replaceAll("\\s+", "_").toLowerCase();
        var endpoint = UriBuilder.fromUri(this.api).path("districts").path(name).build();

        return this.client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(district))
                .readEntity(District.class);
    }

    public List<Location> getLocations() {
        var endpoint = UriBuilder.fromUri(this.api).path("locations").build();

        return this.client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<>() {});
    }

    public Location getLocation(final int id) {
        var endpoint =
                UriBuilder.fromUri(this.api).path("locations").path(String.valueOf(id)).build();

        return this.client.target(endpoint).request(MediaType.APPLICATION_JSON).get(Location.class);
    }

    public Location createLocation(final Location location) {
        var endpoint = UriBuilder.fromUri(this.api).path("locations").build();

        return this.client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(location))
                .readEntity(Location.class);
    }

    public Location updateLocation(final Location location) {
        var id = String.valueOf(location.getID());
        var endpoint = UriBuilder.fromUri(this.api).path("locations").path(id).build();

        return this.client
                .target(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(location))
                .readEntity(Location.class);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static void main(final String[] args) {
        var http =
                new HttpClient.Builder().protocol("http").hostname("localhost").port(8090).build();

        var district = http.getDistrict("   Porto    ");
        System.out.println("district = " + district);

        var districts = http.getDistricts();
        for (var d : districts) {
            System.out.println("d = " + d);
        }

        var porto = new District("Porto", 50, 50, 50);
        System.out.println("porto = " + http.updateDistrict(porto));

        var ilhaMadeira = http.createDistrict(new District("Ilha madeira", 3, 2, 1));
        System.out.println("ilha_madeira = " + ilhaMadeira);

        var casa = new Location("braga", 0, 0, 10);
        System.out.println("Casa: " + http.updateLocation(casa));
    }
}
