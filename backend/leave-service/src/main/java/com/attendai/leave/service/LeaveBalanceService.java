package com.attendai.leave.service;

import com.attendai.leave.dto.LeaveBalanceResponse;
import com.attendai.leave.entity.LeaveBalance;
import com.attendai.leave.entity.LeaveType;
import com.attendai.leave.exception.ResourceNotFoundException;
import com.attendai.leave.repository.LeaveBalanceRepository;
import com.attendai.leave.repository.LeaveRequestRepository;
import com.attendai.leave.repository.LeaveTypeRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaveBalanceService {

    private final LeaveBalanceRepository balanceRepo;
    private final LeaveTypeRepository typeRepo;
    private final LeaveRequestRepository requestRepo;

    public LeaveBalanceService(LeaveBalanceRepository balanceRepo,
                                LeaveTypeRepository typeRepo,
                                LeaveRequestRepository requestRepo) {
        this.balanceRepo = balanceRepo;
        this.typeRepo = typeRepo;
        this.requestRepo = requestRepo;
    }

    /**
     * Returns the current year's balance for every active leave type.
     * Creates missing balance records on the fly (e.g. for a newly onboarded employee).
     */
    @Transactional
    public List<LeaveBalanceResponse> getOrCreateBalances(Long employeeId) {
        int year = LocalDate.now().getYear();
        List<LeaveType> activeTypes = typeRepo.findByActiveTrue();
        List<LeaveBalanceResponse> result = new ArrayList<>();

        for (LeaveType lt : activeTypes) {
            LeaveBalance lb = balanceRepo
                    .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, lt.getId(), year)
                    .orElseGet(() -> createBalance(employeeId, lt, year));

            // Sync used/pending from actual requests
            lb.setUsedDays(requestRepo.sumApprovedDays(employeeId, lt.getId(), year));
            lb.setPendingDays(requestRepo.sumPendingDays(employeeId, lt.getId(), year));
            balanceRepo.save(lb);

            result.add(LeaveBalanceResponse.from(lb));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getForYear(Long employeeId, int year) {
        return balanceRepo.findByEmployeeIdAndYear(employeeId, year)
                .stream().map(LeaveBalanceResponse::from).toList();
    }

    /**
     * Initialize balances for all active leave types for a new employee.
     */
    @Transactional
    public void initializeForEmployee(Long employeeId) {
        int year = LocalDate.now().getYear();
        typeRepo.findByActiveTrue().forEach(lt -> {
            if (balanceRepo.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, lt.getId(), year).isEmpty()) {
                createBalance(employeeId, lt, year);
            }
        });
    }

    /**
     * Refresh a single balance record (called after approve/reject/cancel).
     */
    @Transactional
    public void refresh(Long employeeId, Long leaveTypeId, int year) {
        LeaveType lt = typeRepo.findById(leaveTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found: " + leaveTypeId));
        LeaveBalance lb = balanceRepo
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseGet(() -> createBalance(employeeId, lt, year));
        lb.setUsedDays(requestRepo.sumApprovedDays(employeeId, leaveTypeId, year));
        lb.setPendingDays(requestRepo.sumPendingDays(employeeId, leaveTypeId, year));
        balanceRepo.save(lb);
    }

    private LeaveBalance createBalance(Long employeeId, LeaveType lt, int year) {
        LeaveBalance lb = new LeaveBalance();
        lb.setEmployeeId(employeeId);
        lb.setLeaveType(lt);
        lb.setYear(year);
        lb.setAllocatedDays(lt.getDefaultDays());
        return balanceRepo.save(lb);
    }
}
