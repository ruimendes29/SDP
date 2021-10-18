package covidtracker.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.text.WordUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "District")
public final class District {
    @NotEmpty private String name;

    @Min(0)
    @NotNull
    private int users;

    @Min(0)
    @NotNull
    private int infected;

    @Min(0)
    @NotNull
    @ApiModelProperty(notes = "Averaged number of people contacted by infected person")
    private double contacted;

    public District() {
        // Jackson deserialization
    }

    public District(@NotEmpty final String name) {
        this.name = name;
    }

    public District(
            @NotEmpty final String name,
            @NotNull final int users,
            @NotNull final int infected,
            @NotNull final double contacted) {
        this.name = name;
        this.users = users;
        this.infected = infected;
        this.contacted = contacted;
    }

    @JsonProperty
    public String getName() {
        return WordUtils.capitalize(name.strip().replaceAll("\\s+", " ").toLowerCase());
    }

    @JsonProperty
    public int getUsers() {
        return this.users;
    }

    @JsonProperty
    public int getInfected() {
        return this.infected;
    }

    @JsonProperty
    public double getContacted() {
        return this.contacted;
    }

    @JsonProperty
    public double getRatio() {
        return (double) this.infected / this.users;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        District district = (District) o;

        return new EqualsBuilder()
                .append(getUsers(), district.getUsers())
                .append(getInfected(), district.getInfected())
                .append(getContacted(), district.getContacted())
                .append(getName(), district.getName())
                .isEquals();
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getUsers())
                .append(getInfected())
                .append(getContacted())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "District{"
                + "name='"
                + name
                + '\''
                + ", users="
                + users
                + ", infected="
                + infected
                + ", contacted="
                + contacted
                + ", ration="
                + this.getRatio()
                + '}';
    }
}
