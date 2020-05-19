package edu.ucsb.cs48.s20.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.ucsb.cs48.s20.demo.services.MembershipService;
import edu.ucsb.cs48.s20.demo.services.ValidEmailService;
import edu.ucsb.cs48.s20.demo.entities.Admin;
import edu.ucsb.cs48.s20.demo.repositories.AdminRepository;
import java.util.Optional;
import javax.validation.Valid;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AdminController {

    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private MembershipService ms;

    private AdminRepository adminRepository;

    @Autowired
    public AdminController(AdminRepository repo) {
        this.adminRepository = repo;
    }

    @GetMapping("/admin")
    public String admin(Model model, OAuth2AuthenticationToken token,
                        RedirectAttributes redirAttrs) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger",
                                         "You do not have permission to access that page");
            return "redirect:/";
        }
        addAdminsFromPropertiesFile();
        model.addAttribute("admins", adminRepository.findAll());
        model.addAttribute("newAdmin", new Admin());
        return "admin/create";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteAdmin(@PathVariable("id") long id, Model model,
                              RedirectAttributes redirAttrs, OAuth2AuthenticationToken token) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger",
                                         "You do not have permission to access that page");
            return "redirect:/";
        }

        Optional<Admin> admin = adminRepository.findById(id);
        if (!admin.isPresent()) {
            redirAttrs.addFlashAttribute("alertDanger", "Admin with that id does not exist.");
        } else {
            String email = admin.get().getEmail();
            if (ms.getAdminEmails().contains(email)) {
                redirAttrs.addFlashAttribute("alertDanger", "Admin " + email + " was set from application properties and cannot be deleted.");
            } else {
                adminRepository.delete(admin.get());
                redirAttrs.addFlashAttribute("alertSuccess", "Admin successfully deleted.");
            }
        }
        model.addAttribute("newAdmin", new Admin());
        model.addAttribute("admins", adminRepository.findAll());
        return "redirect:/admin";
    }

    @PostMapping("/admin/add")
    public String addAdmin(@Valid Admin admin, BindingResult result, Model model,
                           RedirectAttributes redirAttrs, OAuth2AuthenticationToken token) {
        String role = ms.role(token);
        if (!role.equals("Admin")) {
            redirAttrs.addFlashAttribute("alertDanger",
                                         "You do not have permission to access that page");
            return "redirect:/";
        }

        boolean errors = false;
        if (!ValidEmailService.validEmail(admin.getEmail())) {
            errors = true;
            redirAttrs.addFlashAttribute("alertDanger", "Invalid email.");
        }
        List<Admin> alreadyExistingAdmins = adminRepository.findByEmail(admin.getEmail());
        if (!alreadyExistingAdmins.isEmpty()) {
            errors = true;
            redirAttrs.addFlashAttribute("alertDanger", "An admin with that email already exists.");
        }
        if (!errors) {
            adminRepository.save(admin);
            model.addAttribute("newAdmin", new Admin());
        } else {
            model.addAttribute("newAdmin", admin);
        }
        model.addAttribute("admins", adminRepository.findAll());
        return "redirect:/admin";
    }

    private void addAdminsFromPropertiesFile() {
        ms.getAdminEmails().forEach((email) -> {
            if (adminRepository.findByEmail(email).isEmpty()) {
                adminRepository.save(new Admin(email, true));
            }
        });
    }
}
