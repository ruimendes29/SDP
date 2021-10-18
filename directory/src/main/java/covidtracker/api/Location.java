package covidtracker.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.text.WordUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "Location")
public final class Location {
    @NotEmpty private String district;

    @NotNull private int x;

    @NotNull private int y;

    @Min(0)
    @NotNull
    private int crowding;

    public Location() {
        // Jackson deserialization
    }

    public Location(@NotEmpty final String district, @NotNull final int x, @NotNull final int y) {
        this.setDistrict(district);
        this.x = x;
        this.y = y;
    }

    public Location(
            @NotEmpty final String district,
            @NotNull final int x,
            @NotNull final int y,
            @Min(0) @NotNull final int crowding) {
        this.setDistrict(district);
        this.x = x;
        this.y = y;
        this.crowding = crowding;
    }

    public void setDistrict(final String district) {
        this.district =
                WordUtils.capitalize(district.strip().replaceAll("\\s+", " ").toLowerCase());
    }

    @JsonProperty
    public int getID() {
        return this.hashCode();
    }

    @JsonProperty
    public String getDistrict() {
        return this.district;
    }

    @JsonProperty
    public int getX() {
        return this.x;
    }

    @JsonProperty
    public int getY() {
        return this.y;
    }

    @JsonProperty
    public int getCrowding() {
        return this.crowding;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return new EqualsBuilder()
                .append(getX(), location.getX())
                .append(getY(), location.getY())
                .append(getDistrict(), location.getDistrict())
                .isEquals();
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getDistrict())
                .append(getX())
                .append(getY())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Location{"
                + "district='"
                + district
                + '\''
                + ", x="
                + x
                + ", y="
                + y
                + ", crowding="
                + crowding
                + '}';
    }
}
