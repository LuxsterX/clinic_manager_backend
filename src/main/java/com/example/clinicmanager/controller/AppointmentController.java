package com.example.clinicmanager.controller;

import com.example.clinicmanager.dto.AppointmentDTO;
import com.example.clinicmanager.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "APIs for managing appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "Get all appointments", description = "Retrieve a list of all appointments")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @Operation(summary = "Get appointments for the logged-in user", description = "Retrieve a list of appointments specific to the logged-in user")
    @GetMapping("/patient/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointments(Principal principal) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForUser(principal.getName()));
    }

    @Operation(summary = "Create a new appointment as a patient", description = "Allows a patient to create a new appointment")
    @PostMapping("/patient/schedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> createAppointmentAsPatient(@RequestBody AppointmentDTO appointmentDTO, Principal principal) {
        try {
            return ResponseEntity.ok(appointmentService.createAppointmentAsPatient(appointmentDTO, principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create appointment: " + e.getMessage());
        }
    }

    @Operation(summary = "Cancel an appointment", description = "Allows a patient to cancel their own appointment by updating the status to CANCELED")
    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long appointmentId, Principal principal) {
        try {
            appointmentService.markAppointmentAsCanceled(appointmentId, principal.getName());
            return ResponseEntity.ok("Appointment canceled successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Appointment not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }
}
