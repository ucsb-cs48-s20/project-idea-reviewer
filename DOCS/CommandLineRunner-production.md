# Running against the production database

By default, the command line runner (documented in [DOCS/CommandLineRunner.md](./CommandLineRunner.md) runs against the localhost temporary H2 database.

If you want it to run with the Heroku production database environment instead, take these
steps:

1.  Go to the Heroku Dashboard of your app, under `Settings`, 
    and then `Reveal Config Vars`.   Copy the current value of 
    `PRODUCTION_PROPERTIES` and paste it somewhere handy.  You'll need to
    copy and paste that value in a moment.
 
2.  Get the jdbc database url for your Heroku app by running the following command
    at a terminal prompt where you have the Heroku CLI installed, and are logged in
    to your Heroku account with `heroku login -i`
    ```
    heroku run -a YOUR_PROJECT_NAME echo \$JDBC_DATABASE_URL
    ```

    Note that `YOUR_PROJECT_NAME` is the name of your app in Heroku,
    e.g. `cs48-s20-s0-t0-prod` or `project-idea-reviewer`.   You may also
    specify the QA version of your app; basically, any Heroku app with a
    Postgres database.

    Have the output of this command (the full output, not just the URL) 
    ready to copy and paste as well.

3.  Now, type the following `export` command at the shell prompt, in the 
    same shell (terminal window) where you are going to run your app
    in a moment (with `mvn spring-boot:run...`)

    Copy/paste the values from steps 1 and 2 above before you press enter.

    ```
    export PRODUCTION_PROPERTIES="
    (paste the PRODUCTION_PROPERTIES from Step 1 here)
    spring.datasource.url=PASTE_URL_FROM_STEP2_HERE
    "
    ```
    
    Basically, you want the `PRODUCTION_PROPERTIES` environment variable to
    be all of the `PRODUCTION_PROPERTIES` from your existing Heroku app, plus
    in addition, you want `spring.datasource.url` set equal to the 
    value of `JDBC_DATABASE_URL` for your Heroku app.
    
    You can check whether it worked by typing
    ```
    echo $PRODUCTION_PROPERTIES
    ```

4. Run mvn spring-boot:run -Dspring-boot.run.arguments="…" and see the data on the heroku app!
