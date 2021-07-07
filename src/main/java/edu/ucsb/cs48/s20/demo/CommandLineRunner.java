package edu.ucsb.cs48.s20.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.entities.Admin;

import edu.ucsb.cs48.s20.demo.services.CSVToObjectService;
import edu.ucsb.cs48.s20.demo.repositories.AdminRepository;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;

@Component
public class CommandLineRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineRunner.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    CSVToObjectService<Student> csvToObjectService;
    
    @Autowired
    private AdminRepository adminRepository;

    // Note: you can pass commandline arguments to spring boot with the following
    // format:
    // mvn spring-boot:run
    // -Dspring-boot.run.arguments="--load-student-file=filename"
    // mvn spring-boot:run -Dspring-boot.run.arguments="--load-student-file=file1
    // --load-student-file=file2"
    // mvn spring-boot:run
    // -Dspring-boot.run.arguments="--add-admin=cgaucho@example.org"

    private HashMap<String, OptionProcessor> options;

    @FunctionalInterface
    private interface OptionProcessor {
        public void processOption(String value);
    }

    public CommandLineRunner() {
        options = new HashMap<String, OptionProcessor>();
        options.put("load-student-file", (s) -> loadStudentFile(s));
        options.put("add-admin", (s) -> setAdmin(s));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Set<String> optionNames = args.getOptionNames();

        if (optionNames.size() == 0) {
            logger.info("No command line arguments to CommandLineRunner");
            return;
        }

        for (String option : optionNames) {
            OptionProcessor optionProcessor = options.get(option);
            if (optionProcessor == null) {
                logger.error("{} is not a recognized command line option; value ignored", option);
            } else {
                List<String> optionValues = args.getOptionValues(option);
                logger.info("processing: {} for values: {}", option, optionValues);
                optionValues.forEach((item) -> optionProcessor.processOption(item));
            }
        }
    }

    public void loadStudentFile(String filename) {
        logger.info("Loading data from {}", filename);

        InputStream targetStream = null;

        try {
            File initialFile = new File(filename);
            targetStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException fnfe) {
            logger.error("File not found: {}, no data loaded.", filename);
            return;
        }

        try (Reader reader = new InputStreamReader(targetStream)) {
            List<Student> students = csvToObjectService.parse(reader, Student.class);
            studentRepository.saveAll(students);
        } catch (IOException ioe) {
            logger.error("Exception: ", ioe);
        }

    }

    public void setAdmin(String adminEmail) {
        logger.info("Adding admin {}", adminEmail);
        Admin newAdmin = new Admin();
        newAdmin.setEmail(adminEmail);
        newAdmin = adminRepository.save(newAdmin);
        logger.info("Admin {} added successfully", newAdmin);
    }

}