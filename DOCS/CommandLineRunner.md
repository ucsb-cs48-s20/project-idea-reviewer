# How to use the `CommandLineRunner`

This repo contains an example of a `CommandLineRunner` in the file:

```
src/main/java/edu/ucsb/cs48/s20/demo/CommandLineRunner.java
```

The `CommandLineRunner` class gives you a "hook" to interact with the Spring Boot environment
at the command line to do one-off jobs.  The sample code shows two functions:

* Loading data from a CSV file into a database table (in this case, the `students` table)
* Adding a single record to a database table (an initial admin)

By default, the command line runner runs against the localhost temporary H2 database.
To run it against the real Postgres database on Heroku instead, follow the 
instructions in  [DOCS/CommandLineRunner-production.md](./CommandLineRunner-production.md) 

# Using the Command Line Runner

To see the available options, look at the code in `CommandLineRunner.java` in the
constructor, where we initialize the `options` hash:

```java
public CommandLineRunner() {
        options = new HashMap<String, OptionProcessor>();
        options.put("load-student-file", (s) -> loadStudentFile(s));
        options.put("add-admin", (s) -> setAdmin(s));
    }
```

In this case, we see that `load-student-file` and `add-admin` are the available options.

## Using `load-student-file`

The `load-student-file` option allows us to upload a CSV file containing students
directly into the Student table.

To use the `load-student-file` option, we run with a command line such as this:

```
mvn spring-boot:run -Dspring-boot.run.arguments="--load-student-file=students.csv" 
```

The argument `students.csv` will be passed to the function `loadStudentFile`, 
and the code in that function will be executed.  This is a conseqence of this
line in the constructor:

```
        options.put("load-student-file", (s) -> loadStudentFile(s));
```

The code for the `loadStudentFile` method appears later in the `CommandLineRunner.java` file

## Using `add-admin`

The `add-admin` option allows us to directly put one record into the admin table
by specifying the email address of the admin.

To use the `add-admin` option, we run with a command line such as this:

```
mvn spring-boot:run -Dspring-boot.run.arguments="--add-admin=cgaucho@ucsb.edu" 
```

The argument `cgaucho@ucsb.edu` will be passed to the function `addAdmin`, 
and the code in that function will be executed.  This is a conseqence of this
line in the constructor:

```
        options.put("add-admin", (s) -> setAdmin(s));
```

The code for the `setAdmin` method appears later in the `CommandLineRunner.java` file

## Running with multiple options

You can run with multiple instances of a given option by separating the options with a space

Example: Add students from multiple sections

```
mvn spring-boot:run -Dspring-boot.run.arguments="--load-student-file=1pm.csv --load-student-file=2pm.csv" 
```

Example: Add multiple admins

```
mvn spring-boot:run -Dspring-boot.run.arguments="--add-admin=cgaucho@ucsb.edu  --add-admin=ldelplaya@ucsb.edu"
```

You can also combine options together by separating them with spaces:

```
mvn spring-boot:run -Dspring-boot.run.arguments="--load-student-file=1pm.csv --load-student-file=2pm.csv --add-admin=cgaucho@ucsb.edu" 
```

# Adding new commands

To add a new command, there are two steps:

1. Write a function that takes a single paramter, a String, which is the value of 
   your command line option.   Model functions include:

   * `public void loadStudentFile(String filename)`
   * `public void setAdmin(String adminEmail)`

  If your function requires any imports or `@Autowired` values (e.g. database
  repository objects), you may need to include those in the class; that will
  depend on what your command is doing.   
  
  For example, `loadStudentFile` depends on an autowired `studentRepository` and
  `csvToObjectService`, while `setAdmin` depends on an autowired `adminRepository`.

2. Add a line in the constructor that indicates the keyword for your new option,
   and specifies a lambda function to pass the `String` argument to a call
   to that function.

   Examples:

   ```
   options.put("load-student-file", (s) -> loadStudentFile(s));
   options.put("add-admin", (s) -> setAdmin(s));
   ``` 

That should be it: once you've completed those two steps, your command line runner
should be ready to go.
