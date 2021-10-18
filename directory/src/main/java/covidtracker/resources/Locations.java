package covidtracker.resources;

import com.codahale.metrics.annotation.Timed;
import covidtracker.api.Location;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/locations", protocols = "http")
@SuppressWarnings("checkstyle:MagicNumber")
public final class Locations {
    private final Map<Integer, Location> locations;

    public Locations() {
        this.locations = new ConcurrentHashMap<>();
    }

    @GET
    @Timed
    @Path("/")
    // spotless:off
    public List<Location> fetch(
        @ApiParam(value = "Sort the result by field", allowableValues = "district,crowding", defaultValue = "crowding")
        @QueryParam("sort") final Optional<String> sort,
        @ApiParam(value = "Limits the size of the result set", defaultValue = "5")
        @QueryParam("limit") final Optional<Integer> top) {
        // spotless:on
        Comparator<Location> comparator;

        switch (sort.orElse("crowding")) {
            case "crowding":
                comparator = Comparator.comparingDouble(d -> d.getCrowding() * -1);
                break;
            case "district":
                comparator = Comparator.comparing(Location::getDistrict);
                break;
            default:
                throw new WebApplicationException("Sort field isn't supported", 400);
        }

        return this.locations.values().stream()
                .sorted(comparator)
                .limit(top.orElse(5))
                .collect(Collectors.toList());
    }

    @GET
    @Timed
    @Path("/{id}")
    public Location fetch(@PathParam("id") final int id) {
        if (!this.locations.containsKey(id)) {
            throw new WebApplicationException("Location doesn't exist", 404);
        }

        return this.locations.get(id);
    }

    @POST
    @Timed
    @Path("/")
    public Location create(@NotNull @Valid final Location location) {
        var id = location.getID();

        if (this.locations.containsKey(id)) {
            throw new WebApplicationException("Location already exists", 409);
        }

        this.locations.put(id, location);

        return this.locations.get(id);
    }

    @PUT
    @Timed
    @Path("/{id}")
    public Location update(@PathParam("id") final int id, @NotNull @Valid final Location location) {
        if (!this.locations.containsKey(id)) {
            this.locations.put(id, location);
        } else {
            var current = this.locations.get(id);
            if (current.getCrowding() < location.getCrowding()) {
                this.locations.put(id, location);
            }
        }

        return this.locations.get(id);
    }

    @DELETE
    @Timed
    @Path("/{id}")
    public Response delete(@PathParam("id") @NotEmpty final int id) {
        if (this.locations.remove(id) == null) {
            throw new WebApplicationException("Location doesn't exist", 404);
        }

        return Response.ok().build();
    }
}
