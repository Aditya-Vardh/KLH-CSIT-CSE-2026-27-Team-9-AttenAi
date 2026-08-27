package com.attendai.employee.service;

import com.attendai.employee.dto.DepartmentRequest;
import com.attendai.employee.dto.DepartmentResponse;
import com.attendai.employee.dto.PagedResponse;
import com.attendai.employee.entity.Department;
import com.attendai.employee.exception.BadRequestException;
import com.attendai.employee.exception.ResourceNotFoundException;
import com.attendai.employee.repository.DepartmentRepository;
import com.attendai.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                              EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<DepartmentResponse> list(String search, Boolean active, Pageable pageable) {
        return PagedResponse.from(
                departmentRepository.search(search, active, pageable)
                        .map(d -> DepartmentResponse.from(d, employeeRepository.countByDepartmentId(d.getId()))));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department dept = findOrThrow(id);
        return DepartmentResponse.from(dept, employeeRepository.countByDepartmentId(id));
    }

    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Department with name '" + request.name() + "' already exists");
        }
        Department dept = new Department();
        mapRequestToEntity(request, dept);
        return DepartmentResponse.from(departmentRepository.save(dept), 0);
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department dept = findOrThrow(id);
        if (!dept.getName().equalsIgnoreCase(request.name())
                && departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Department with name '" + request.name() + "' already exists");
        }
        mapRequestToEntity(request, dept);
        return DepartmentResponse.from(departmentRepository.save(dept),
                employeeRepository.countByDepartmentId(id));
    }

    public void deactivate(Long id) {
        Department dept = findOrThrow(id);
        dept.setActive(false);
        departmentRepository.save(dept);
    }

    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        long count = employeeRepository.countByDepartmentId(id);
        if (count > 0) {
            throw new BadRequestException("Cannot delete department with " + count + " active employees");
        }
        departmentRepository.deleteById(id);
    }

    private Department findOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private void mapRequestToEntity(DepartmentRequest req, Department dept) {
        dept.setName(req.name());
        dept.setDescription(req.description());
        dept.setHeadUserId(req.headUserId());
    }
}
