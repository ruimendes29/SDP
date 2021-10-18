package covidtracker.resources;

import com.codahale.metrics.annotation.Timed;
import covidtracker.api.District;
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

@Path("/districts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/districts", protocols = "http")
@SuppressWarnings("checkstyle:MagicNumber")
public final class Districts {
    private final Map<String, District> districts;

    public Districts() {
        this.districts = new ConcurrentHashMap<>();
    }

    @GET
    @Timed
    @Path("/")
    // spotless:off
    public List<District> fetch(
        @ApiParam(value = "Sort the result by field",
            allowableValues = "ratio, name, users, infected, contacted",
            defaultValue = "ratio")
        @QueryParam("sort") final Optional<String> sort,
        @ApiParam(value = "Limits the size of the result set", defaultValue = "5")
        @QueryParam("limit") final Optional<Integer> top) {
        // spotless:on
        Comparator<District> comparator;

        switch (sort.orElse("ratio")) {
            case "ratio":
                comparator = Comparator.comparingDouble(d -> d.getRatio() * -1);
                break;
            case "name":
                comparator = Comparator.comparing(District::getName);
                break;
            case "users":
                comparator = Comparator.comparingInt(d -> d.getUsers() * -1);
                break;
            case "infected":
                comparator = Comparator.comparingInt(d -> d.getInfected() * -1);
                break;
            case "contacted":
                comparator = Comparator.comparingDouble(d -> d.getContacted() * -1);
                break;
            default:
                throw new WebApplicationException("Sort field isn't supported", 400);
        }

        return this.districts.values().stream()
                .sorted(comparator)
                .limit(top.orElse(5))
                .collect(Collectors.toList());
    }

    @GET
    @Timed
    @Path("/{district}")
    public District fetch(@PathParam("district") @NotEmpty final String name) {
        if (!this.districts.containsKey(name.toLowerCase())) {
            throw new WebApplicationException("District doesn't exist", 404);
        }

        return this.districts.get(name.toLowerCase());
    }

    @POST
    @Timed
    @Path("/")
    public District create(@NotNull @Valid final District district) {
        var name = district.getName().strip().replaceAll("\\s+", "_").toLowerCase();

        if (this.districts.containsKey(name)) {
            throw new WebApplicationException("District already exists", 409);
        }

        this.districts.put(name, district);

        return this.districts.get(name);
    }

    @PUT
    @Timed
    @Path("/{district}")
    public District update(
            @PathParam("district") @NotEmpty final String name,
            @NotNull @Valid final District district) {
        if (!this.districts.containsKey(name.toLowerCase())) {
            throw new WebApplicationException("District doesn't exist", 404);
        }

        this.districts.put(name.toLowerCase(), district);

        return this.districts.get(name.toLowerCase());
    }

    @DELETE
    @Timed
    @Path("/{district}")
    public Response delete(@PathParam("district") @NotEmpty final String name) {
        if (this.districts.remove(name.toLowerCase()) == null) {
            throw new WebApplicationException("District doesn't exist", 404);
        }

        return Response.ok().build();
    }
}
