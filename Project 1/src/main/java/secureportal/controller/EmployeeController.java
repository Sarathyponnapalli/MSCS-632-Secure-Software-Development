package secureportal.controller;

import secureportal.model.Employee;
import secureportal.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Employee> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Employee> searchByLastName(@RequestParam String lastName) {
        return repository.searchByLastName(lastName);
    }

    @GetMapping("/search/keyword")
    public List<Employee> searchByKeyword(@RequestParam String q) {
        return repository.searchByKeyword(q);
    }

    @GetMapping("/department/{deptId}")
    public List<Employee> getByDepartment(@PathVariable Integer deptId) {
        return repository.findByDepartmentId(deptId);
    }

    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody Employee emp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(emp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id,
                                            @Valid @RequestBody Employee updated) {
        return repository.findById(id).map(existing -> {
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setEmail(updated.getEmail());
            existing.setPhoneNumber(updated.getPhoneNumber());
            existing.setJobId(updated.getJobId());
            existing.setSalary(updated.getSalary());
            existing.setDepartmentId(updated.getDepartmentId());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
