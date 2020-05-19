package edu.ucsb.cs48.s20.demo.controllers;

import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.entities.Review;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;
import edu.ucsb.cs48.s20.demo.repositories.ReviewRepository;
import edu.ucsb.cs48.s20.demo.services.CSVToObjectService;
import edu.ucsb.cs48.s20.demo.services.MembershipService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentsController {

    private Logger logger = LoggerFactory.getLogger(StudentsController.class);

    @Autowired
    private MembershipService ms;

    private StudentRepository studentRepository;

    private ReviewRepository reviewRepository;

    @Autowired
    CSVToObjectService<Student> csvToObjectService;

    @Autowired
    public StudentsController(StudentRepository repo, ReviewRepository reviewRepository) {
        this.studentRepository = repo;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/students")
    public String students(Model model, OAuth2AuthenticationToken token, RedirectAttributes redirAttrs) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger", "You do not have permission to access that page");
            return "redirect:/";
        }
        model.addAttribute("students", studentRepository.findAll());
        return "students/index";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteAdmin(@PathVariable("id") Long id, Model model, RedirectAttributes redirAttrs,
                              OAuth2AuthenticationToken token) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger", "You do not have permission to access that page");
            return "redirect:/";
        }

        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (!optionalStudent.isPresent()) {
            redirAttrs.addFlashAttribute("alertDanger", "User with that id does not exist.");
        } else {
            Student student = optionalStudent.get();
            String email = student.getEmail();
            studentRepository.delete(student);
            redirAttrs.addFlashAttribute("alertSuccess", "Student " + email + "successfully deleted.");
        }
        model.addAttribute("students", studentRepository.findAll());
        return "redirect:/students";
    }

    @PostMapping("/students/upload")
    public String uploadCSV(@RequestParam("csv") MultipartFile csv, OAuth2AuthenticationToken token, RedirectAttributes redirAttrs) {
        String role = ms.role(token);
        if (!(role.equals("Admin"))) {
            redirAttrs.addFlashAttribute("alertDanger", "You do not have permission to access that page");
            return "redirect:/";
        }
        try(Reader reader = new InputStreamReader(csv.getInputStream())) {
            List<Student> students = csvToObjectService.parse(reader, Student.class);
            studentRepository.saveAll(students);
        } catch(IOException e) {
            logger.error(e.toString());
        } catch(RuntimeException a) {
            logger.error("Exception: ",a);
            redirAttrs.addFlashAttribute("alertDanger", "Please enter a correct csv file.");
            return "redirect:/students";
        }

        return "redirect:/students";
    }

}
