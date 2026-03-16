package secureportal.repository;

import secureportal.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByLastName(String lastName);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(Integer departmentId);

    @Query("SELECT e FROM Employee e WHERE e.lastName = :name")
    List<Employee> searchByLastName(@Param("name") String name);

    @Query("SELECT e FROM Employee e WHERE e.departmentId = :deptId AND e.salary > :minSalary")
    List<Employee> findByDeptAndMinSalary(@Param("deptId") Integer deptId,
                                          @Param("minSalary") Double minSalary);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchByKeyword(@Param("keyword") String keyword);
}
