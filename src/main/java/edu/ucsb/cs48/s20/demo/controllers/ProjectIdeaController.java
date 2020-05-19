package edu.ucsb.cs48.s20.demo.controllers;

import edu.ucsb.cs48.s20.demo.advice.StudentFlowAdvice;
import edu.ucsb.cs48.s20.demo.entities.ProjectIdea;
import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.entities.Review;
import edu.ucsb.cs48.s20.demo.formbeans.Idea;
import edu.ucsb.cs48.s20.demo.repositories.ProjectIdeaRepository;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;
import edu.ucsb.cs48.s20.demo.repositories.ReviewRepository;
import edu.ucsb.cs48.s20.demo.services.CSVToObjectService;
import edu.ucsb.cs48.s20.demo.services.MembershipService;

import java.util.Optional;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProjectIdeaController {

    private Logger logger = LoggerFactory.getLogger(ProjectIdeaController.class);

    @Autowired
    private MembershipService ms;

    @Autowired
    private StudentFlowAdvice studentFlowAdvice;

    private StudentRepository studentRepository;

    private ProjectIdeaRepository projectIdeaRepository;

    private ReviewRepository reviewRepository;

    @Autowired
    CSVToObjectService<Student> csvToObjectService;

    @Autowired
    public ProjectIdeaController(ProjectIdeaRepository projectIdeaRepository, StudentRepository studentRepository,
                                 ReviewRepository reviewRepository) {
        this.studentRepository = studentRepository;
        this.projectIdeaRepository = projectIdeaRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/ideas")
    public String ideas(Model model, OAuth2AuthenticationToken token, RedirectAttributes redirAttrs) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger", "You do not have permission to access that page");
            return "redirect:/";
        }
        model.addAttribute("ideas", projectIdeaRepository.findAll());
        return "ideas/index";
    }

    @PostMapping("/ideas/delete/{id}")
    public String deleteIdea(@PathVariable("id") Long id, Model model, RedirectAttributes redirAttrs,
                             OAuth2AuthenticationToken token) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger", "You do not have permission to access that page");
            return "redirect:/";
        }

        Optional<ProjectIdea> optionalIdea = projectIdeaRepository.findById(id);
        if (!optionalIdea.isPresent()) {
            redirAttrs.addFlashAttribute("alertDanger", "ProjectIdea with that id does not exist.");
        } else {
            ProjectIdea idea = optionalIdea.get();
            String title = idea.getTitle();
            Student student = idea.getStudent();
            String name = idea.getStudent().getFname() + " " + idea.getStudent().getLname();
            projectIdeaRepository.delete(idea);
            studentRepository.save(student);
            redirAttrs.addFlashAttribute("alertSuccess",
                                         "Idea " + title + " for student " + name + " successfully deleted.");
        }
        model.addAttribute("ideas", projectIdeaRepository.findAll());
        return "redirect:/ideas";
    }

    @PostMapping("/ideas/add")
    public String addIdea(@ModelAttribute("idea") Idea idea, Model model, OAuth2AuthenticationToken token,
                          BindingResult bindingResult, RedirectAttributes redirAttrs) {
        String role = ms.role(token);
        if (!role.equals("Admin") && !role.equals("Student")) {
            redirAttrs.addFlashAttribute("alertDanger", "You do not have permission to access that page");
            return "redirect:/";
        }

        boolean errors = false;

        model.addAttribute("titleHasErrors", false);
        model.addAttribute("detailHasErrors", false);

        String titleErrors = "";
        String detailErrors = "";

        if (idea.getTitle() == null || idea.getTitle().length() < ProjectIdea.TITLE_CHAR_MIN) {
            int titleLength = (idea.getTitle()==null ? 0 : idea.getTitle().length());
            titleErrors += "Title is too short (" + ProjectIdea.TITLE_CHAR_MIN + " characters minimum). Currently: " + titleLength + " chars.";
        }

        if (idea.getTitle() != null && idea.getTitle().length() > ProjectIdea.TITLE_CHAR_MAX) {
            titleErrors += "Please limit your title to " + ProjectIdea.TITLE_CHAR_MAX  + " chars or fewer. Currently: " + idea.getTitle().length() + " chars. ";
        }

        if (!titleErrors.equals("")) {
            model.addAttribute("titleErrors",titleErrors);
            model.addAttribute("titleHasErrors", true);
            errors = true;
        }

        if (idea.getDetails() == null || idea.getDetails().length() < ProjectIdea.DETAILS_CHAR_MIN ) {
            int detailsLength = (idea.getDetails()==null ? 0 : idea.getDetails().length());
            detailErrors += "Please add some more detail (at least " + ProjectIdea.DETAILS_CHAR_MIN + " characters). Currently: " + detailsLength + " chars.";
        }

        if (idea.getDetails() != null && idea.getDetails().length() > ProjectIdea.DETAILS_CHAR_MAX ) {
            detailErrors += "Please limit your detailed desription to " + ProjectIdea.DETAILS_CHAR_MAX  + " chars or fewer (currently: " + idea.getDetails().length() + " chars). ";
        }

        if (!detailErrors.equals("")) {
            model.addAttribute("detailErrors",detailErrors);
            model.addAttribute("detailHasErrors", true);
            errors = true;
        }

        if (!errors) {
            Student student = studentFlowAdvice.getStudent(token);
            ProjectIdea projectIdea = new ProjectIdea();
            projectIdea.setStudent(student);
            student.setProjectIdea(projectIdea);
            projectIdea.setTitle(idea.getTitle());
            projectIdea.setDetails(idea.getDetails());
            projectIdeaRepository.save(projectIdea);
            studentRepository.save(student);
            return "redirect:/reviews/perform";

        }

        model.addAttribute("idea", idea);

        logger.info("leaving ProjectIdeaController addIdea:");
        logger.info("idea" + idea);

        return "index";

    }

}
