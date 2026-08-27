package com.attendai.employee.service;

import com.attendai.employee.dto.EmployeeRequest;
import com.attendai.employee.dto.EmployeeResponse;
import com.attendai.employee.dto.EmployeeSummary;
import com.attendai.employee.dto.PagedResponse;
import com.attendai.employee.entity.Department;
import com.attendai.employee.entity.Designation;
import com.attendai.employee.entity.Employee;
import com.attendai.employee.entity.EmployeeStatus;
import com.attendai.employee.exception.BadRequestException;
import com.attendai.employee.exception.ResourceNotFoundException;
import com.attendai.employee.repository.DepartmentRepository;
import com.attendai.employee.repository.DesignationRepository;
import com.attendai.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           DesignationRepository designationRepository,
                           AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> list(String search, Long departmentId,
                                                Long designationId, EmployeeStatus status,
                                                Pageable pageable) {
        return PagedResponse.from(
                employeeRepository.search(search, departmentId, designationId, status, pageable)
                        .map(EmployeeResponse::from));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return EmployeeResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getByUserId(Long userId) {
        return EmployeeResponse.from(
                employeeRepository.findByUserId(userId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Employee not found for userId: " + userId)));
    }

    @Transactional(readOnly = true)
    public EmployeeSummary getSummaryById(Long id) {
        Employee e = findOrThrow(id);
        return toSummary(e);
    }

    @Transactional(readOnly = true)
    public EmployeeSummary getSummaryByUserId(Long userId) {
        Employee e = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found for userId: " + userId));
        return toSummary(e);
    }

    public EmployeeResponse create(EmployeeRequest request, String actorEmail, String actorRole) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new BadRequestException("Employee code already exists: " + request.employeeCode());
        }
        Employee emp = new Employee();
        mapRequestToEntity(request, emp);
        Employee saved = employeeRepository.save(emp);
        auditLogService.record(actorEmail, actorRole, "EMPLOYEE_CREATED", "Employee",
                saved.getId(),
                "{\"name\":\"" + saved.getFirstName() + " " + saved.getLastName() + "\","
                        + "\"code\":\"" + saved.getEmployeeCode() + "\"}");
        return EmployeeResponse.from(saved);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request, String actorEmail, String actorRole) {
        Employee emp = findOrThrow(id);

        if (!emp.getEmail().equalsIgnoreCase(request.email())
                && employeeRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }
        if (!emp.getEmployeeCode().equalsIgnoreCase(request.employeeCode())
                && employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new BadRequestException("Employee code already exists: " + request.employeeCode());
        }
        mapRequestToEntity(request, emp);
        Employee saved = employeeRepository.save(emp);
        auditLogService.record(actorEmail, actorRole, "EMPLOYEE_UPDATED", "Employee",
                saved.getId(),
                "{\"name\":\"" + saved.getFirstName() + " " + saved.getLastName() + "\"}");
        return EmployeeResponse.from(saved);
    }

    public EmployeeResponse patch(Long id, EmployeeRequest request, String actorEmail, String actorRole) {
        // Reuses full update — caller sends only changed fields, validation still applies
        return update(id, request, actorEmail, actorRole);
    }

    public void delete(Long id, String actorEmail, String actorRole) {
        Employee emp = findOrThrow(id);
        auditLogService.record(actorEmail, actorRole, "EMPLOYEE_DELETED", "Employee", id,
                "{\"name\":\"" + emp.getFirstName() + " " + emp.getLastName() + "\"}");
        employeeRepository.deleteById(id);
    }

    // ── Legacy no-actor overloads (used by internal/Feign calls) ─────────────

    public EmployeeResponse create(EmployeeRequest request) {
        return create(request, "system", null);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        return update(id, request, "system", null);
    }

    public void delete(Long id) {
        delete(id, "system", null);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private void mapRequestToEntity(EmployeeRequest req, Employee emp) {
        emp.setUserId(req.userId());
        emp.setEmployeeCode(req.employeeCode());
        emp.setFirstName(req.firstName());
        emp.setLastName(req.lastName());
        emp.setEmail(req.email().toLowerCase());
        emp.setPhone(req.phone());
        emp.setAddress(req.address());
        emp.setDateOfBirth(req.dateOfBirth());
        emp.setJoiningDate(req.joiningDate());

        if (req.status() != null) {
            emp.setStatus(req.status());
        }
        if (req.profilePicture() != null) {
            emp.setProfilePicture(req.profilePicture());
        }
        if (req.annualLeaveQuota() != null) {
            emp.setAnnualLeaveQuota(req.annualLeaveQuota());
        }

        if (req.departmentId() != null) {
            Department dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + req.departmentId()));
            emp.setDepartment(dept);
        } else {
            emp.setDepartment(null);
        }

        if (req.designationId() != null) {
            Designation desig = designationRepository.findById(req.designationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Designation not found with id: " + req.designationId()));
            emp.setDesignation(desig);
        } else {
            emp.setDesignation(null);
        }
    }

    private EmployeeSummary toSummary(Employee e) {
        return new EmployeeSummary(
                e.getId(), e.getUserId(), e.getEmployeeCode(),
                e.getFirstName() + " " + e.getLastName(),
                e.getEmail(),
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                e.getDesignation() != null ? e.getDesignation().getTitle() : null,
                e.getAnnualLeaveQuota());
    }
}
