package covidtracker;

import covidtracker.health.DistrictHealthCheck;
import covidtracker.resources.Districts;
import covidtracker.resources.Locations;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public final class App extends Application<AppConfiguration> {
    @Override
    public String getName() {
        return "Covid Tracker - Directory";
    }

    public static void main(final String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(final Bootstrap<AppConfiguration> bootstrap) {
        // Environmental variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor()));

        // Swagger documentation
        bootstrap.addBundle(
                new SwaggerBundle<AppConfiguration>() {
                    @Override
                    protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
                            final AppConfiguration config) {
                        return config.getSwaggerBundleConfiguration();
                    }
                });

        // Serve static files from resources folder
        bootstrap.addBundle(new AssetsBundle("/static/", "/"));
    }

    @Override
    public void run(final AppConfiguration configuration, final Environment environment) {
        environment.jersey().register(new Districts());
        environment.jersey().register(new Locations());
        environment.healthChecks().register("DistrictResource", new DistrictHealthCheck());
    }
}
