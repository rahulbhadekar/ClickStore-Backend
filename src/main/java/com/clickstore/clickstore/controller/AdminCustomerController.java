
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.AdminCustomerResponse;
import com.clickstore.clickstore.service.AdminCustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(
            AdminCustomerService adminCustomerService
    ) {
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public List<AdminCustomerResponse> getAllCustomers() {
        return adminCustomerService.getAllCustomers();
    }
}
