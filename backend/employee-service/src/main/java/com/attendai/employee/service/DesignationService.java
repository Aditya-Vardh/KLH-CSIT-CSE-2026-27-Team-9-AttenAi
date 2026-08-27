package com.attendai.employee.service;

import com.attendai.employee.dto.DesignationRequest;
import com.attendai.employee.dto.DesignationResponse;
import com.attendai.employee.dto.PagedResponse;
import com.attendai.employee.entity.Designation;
import com.attendai.employee.exception.BadRequestException;
import com.attendai.employee.exception.ResourceNotFoundException;
import com.attendai.employee.repository.DesignationRepository;
import com.attendai.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;

    public DesignationService(DesignationRepository designationRepository,
                               EmployeeRepository employeeRepository) {
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<DesignationResponse> list(String search, Boolean active, Pageable pageable) {
        return PagedResponse.from(
                designationRepository.search(search, active, pageable)
                        .map(d -> DesignationResponse.from(d, employeeRepository.countByDesignationId(d.getId()))));
    }

    @Transactional(readOnly = true)
    public DesignationResponse getById(Long id) {
        Designation desig = findOrThrow(id);
        return DesignationResponse.from(desig, employeeRepository.countByDesignationId(id));
    }

    public DesignationResponse create(DesignationRequest request) {
        if (designationRepository.existsByTitleIgnoreCase(request.title())) {
            throw new BadRequestException("Designation with title '" + request.title() + "' already exists");
        }
        Designation desig = new Designation();
        mapRequestToEntity(request, desig);
        return DesignationResponse.from(designationRepository.save(desig), 0);
    }

    public DesignationResponse update(Long id, DesignationRequest request) {
        Designation desig = findOrThrow(id);
        if (!desig.getTitle().equalsIgnoreCase(request.title())
                && designationRepository.existsByTitleIgnoreCase(request.title())) {
            throw new BadRequestException("Designation with title '" + request.title() + "' already exists");
        }
        mapRequestToEntity(request, desig);
        return DesignationResponse.from(designationRepository.save(desig),
                employeeRepository.countByDesignationId(id));
    }

    public void deactivate(Long id) {
        Designation desig = findOrThrow(id);
        desig.setActive(false);
        designationRepository.save(desig);
    }

    public void delete(Long id) {
        if (!designationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Designation not found with id: " + id);
        }
        long count = employeeRepository.countByDesignationId(id);
        if (count > 0) {
            throw new BadRequestException("Cannot delete designation assigned to " + count + " employees");
        }
        designationRepository.deleteById(id);
    }

    private Designation findOrThrow(Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + id));
    }

    private void mapRequestToEntity(DesignationRequest req, Designation desig) {
        desig.setTitle(req.title());
        desig.setDescription(req.description());
        desig.setGrade(req.grade());
    }
}
