package covidtracker.health;

import com.codahale.metrics.health.HealthCheck;
import covidtracker.api.District;

public final class DistrictHealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        new District("Braga", 2, 2, 1);
        return Result.healthy();
    }
}
