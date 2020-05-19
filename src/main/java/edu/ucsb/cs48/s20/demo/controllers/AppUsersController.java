package edu.ucsb.cs48.s20.demo.controllers;

import edu.ucsb.cs48.s20.demo.entities.AppUser;
import edu.ucsb.cs48.s20.demo.repositories.AppUserRepository;
import edu.ucsb.cs48.s20.demo.services.MembershipService;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppUsersController {

    private Logger logger = LoggerFactory.getLogger(AppUsersController.class);

    @Autowired
    private MembershipService ms;

    private AppUserRepository appUserRepository;

    @Autowired
    public AppUsersController(AppUserRepository repo) {
        this.appUserRepository = repo;
    }

    @GetMapping("/users")
    public String users(Model model, OAuth2AuthenticationToken token,
                        RedirectAttributes redirAttrs) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger",
                                         "You do not have permission to access that page");
            return "redirect:/";
        }
        model.addAttribute("users", appUserRepository.findAll());
        return "users/index";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteAdmin(@PathVariable("id") Long id, Model model,
                              RedirectAttributes redirAttrs, OAuth2AuthenticationToken token) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger",
                                         "You do not have permission to access that page");
            return "redirect:/";
        }

        Optional<AppUser> optionalAppUser = appUserRepository.findById(id);
        if (!optionalAppUser.isPresent()) {
            redirAttrs.addFlashAttribute("alertDanger", "User with that id does not exist.");
        } else {
            AppUser appUser = optionalAppUser.get();
            String email = appUser.getEmail();
            String curEmail = ms.email(token);

            if (email.equals(curEmail)) {
                redirAttrs.addFlashAttribute("alertDanger", "Cannot delete the current user");
            } else {
                appUserRepository.delete(appUser);
                redirAttrs.addFlashAttribute("alertSuccess", "User " + email + "successfully deleted.");
            }
        }
        model.addAttribute("users", appUserRepository.findAll());
        return "redirect:/users";
    }

}
