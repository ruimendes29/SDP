### :warning: Project dependencies

You need to have a GitLab Personal Token. You can get one
[here](https://docs.gitlab.com/12.10/ee/user/profile/personal_access_tokens.html).

After filling the `.env` file with `GITLAB_PERSONAL_ACCESS_TOKEN` setup project
dependencies.

```bash
bin/setup dependencies
```

You can try to fix this by manually downloading the correct jar from
[here](https://gitlab.com/mieiuminho/sdp/paradigmas/covid-tracker.directory/-/packages)
and running the following command.

```bash
bin/mvnw install:install-file \
      -DgroupId=pt.uminho.paradigmas -DartifactId=covidtracker.directory \
      -Dversion=<VERSION IN POM> -Dpackaging=jar \
      -Dfile=/path/to/file
```

