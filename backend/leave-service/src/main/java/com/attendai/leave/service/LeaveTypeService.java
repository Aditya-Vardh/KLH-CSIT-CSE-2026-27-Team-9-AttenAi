package com.attendai.leave.service;

import com.attendai.leave.dto.LeaveTypeRequest;
import com.attendai.leave.dto.LeaveTypeResponse;
import com.attendai.leave.entity.LeaveType;
import com.attendai.leave.exception.BadRequestException;
import com.attendai.leave.exception.ResourceNotFoundException;
import com.attendai.leave.repository.LeaveTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaveTypeService {

    private final LeaveTypeRepository repo;

    public LeaveTypeService(LeaveTypeRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> listActive() {
        return repo.findByActiveTrue().stream().map(LeaveTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> listAll() {
        return repo.findAll().stream().map(LeaveTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LeaveTypeResponse getById(Long id) {
        return LeaveTypeResponse.from(findOrThrow(id));
    }

    public LeaveTypeResponse create(LeaveTypeRequest req) {
        if (repo.existsByNameIgnoreCase(req.name())) {
            throw new BadRequestException("Leave type '" + req.name() + "' already exists");
        }
        LeaveType lt = new LeaveType();
        map(req, lt);
        return LeaveTypeResponse.from(repo.save(lt));
    }

    public LeaveTypeResponse update(Long id, LeaveTypeRequest req) {
        LeaveType lt = findOrThrow(id);
        if (!lt.getName().equalsIgnoreCase(req.name()) && repo.existsByNameIgnoreCase(req.name())) {
            throw new BadRequestException("Leave type '" + req.name() + "' already exists");
        }
        map(req, lt);
        return LeaveTypeResponse.from(repo.save(lt));
    }

    public void deactivate(Long id) {
        LeaveType lt = findOrThrow(id);
        lt.setActive(false);
        repo.save(lt);
    }

    private LeaveType findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found: " + id));
    }

    private void map(LeaveTypeRequest req, LeaveType lt) {
        lt.setName(req.name());
        lt.setDescription(req.description());
        lt.setDefaultDays(req.defaultDays());
        lt.setCarryOver(req.carryOver());
        lt.setAllowNegative(req.allowNegative());
        lt.setRequiresDocument(req.requiresDocument());
    }
}
