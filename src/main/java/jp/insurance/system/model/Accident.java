package jp.insurance.system.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Accident {
    private Long id;
    private Long policyId;
    private LocalDate occurredAt;
    private String place;
    private String description;
    private AccidentStatus status;
    private LocalDateTime lastContactedAt;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // For display (JOIN with policies)
    private String policyNumber;
    private String customerName;

    // Constructors
    public Accident() {}

    public Accident(Long id, Long policyId, LocalDate occurredAt, String place, 
                    String description, AccidentStatus status) {
        this.id = id;
        this.policyId = policyId;
        this.occurredAt = occurredAt;
        this.place = place;
        this.description = description;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public LocalDate getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDate occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccidentStatus getStatus() {
        return status;
    }

    public void setStatus(AccidentStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastContactedAt() {
        return lastContactedAt;
    }

    public void setLastContactedAt(LocalDateTime lastContactedAt) {
        this.lastContactedAt = lastContactedAt;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}