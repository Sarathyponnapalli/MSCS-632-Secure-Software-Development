package secureportal.controller;

import secureportal.model.Employee;
import secureportal.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    private final EmployeeRepository repository;

    public WebController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities());
        model.addAttribute("employeeCount", repository.count());
        return "dashboard";
    }

    @GetMapping("/employees")
    public String listEmployees(@RequestParam(required = false) String search,
                                 Model model, Authentication auth) {
        List<Employee> employees;
        if (search != null && !search.isBlank()) {
            employees = repository.searchByKeyword(search.trim());
            model.addAttribute("search", search);
        } else {
            employees = repository.findAll();
        }
        model.addAttribute("employees", employees);
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin",
                auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        return "employees";
    }

    @GetMapping("/employees/add")
    public String addForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("action", "Add");
        return "employee-form";
    }

    @GetMapping("/employees/edit/{id}")
    public String editForm(@PathVariable Long id, Model model,
                            RedirectAttributes redir) {
        return repository.findById(id).map(emp -> {
            model.addAttribute("employee", emp);
            model.addAttribute("action", "Edit");
            return "employee-form";
        }).orElseGet(() -> {
            redir.addFlashAttribute("error", "Employee not found.");
            return "redirect:/employees";
        });
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@Valid @ModelAttribute Employee employee,
                                BindingResult result, RedirectAttributes redir) {
        if (result.hasErrors()) return "employee-form";
        repository.save(employee);
        redir.addFlashAttribute("success", "Employee saved.");
        return "redirect:/employees";
    }

    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redir) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            redir.addFlashAttribute("success", "Employee deleted.");
        } else {
            redir.addFlashAttribute("error", "Employee not found.");
        }
        return "redirect:/employees";
    }
}
