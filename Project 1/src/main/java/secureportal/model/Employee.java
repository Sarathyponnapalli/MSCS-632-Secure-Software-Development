package secureportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "first_name", length = 50)
    @Size(max = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "job_id", length = 20)
    private String jobId;

    @Column(name = "salary")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be > 0")
    private Double salary;

    @Column(name = "department_id")
    private Integer departmentId;

    // Constructors
    public Employee() {}

    public Employee(String firstName, String lastName, String email,
                    String phoneNumber, String jobId,
                    Double salary, Integer departmentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.jobId = jobId;
        this.salary = salary;
        this.departmentId = departmentId;
    }

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long id) { this.employeeId = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String v) { this.phoneNumber = v; }

    public String getJobId() { return jobId; }
    public void setJobId(String v) { this.jobId = v; }

    public Double getSalary() { return salary; }
    public void setSalary(Double v) { this.salary = v; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer v) { this.departmentId = v; }
}
