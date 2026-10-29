
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.dto.AdminCustomerResponse;
import com.clickstore.clickstore.entity.User;
import com.clickstore.clickstore.entity.UserContact;
import com.clickstore.clickstore.repository.UserContactRepository;
import com.clickstore.clickstore.repository.UserRepository;
import com.clickstore.clickstore.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final UserRoleRepository userRoleRepository;

    public AdminCustomerService(
            UserRepository userRepository,
            UserContactRepository userContactRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCustomerResponse> getAllCustomers() {
        return userRoleRepository.findCustomerUserIds()
                .stream()
                .map(userId -> {
                    User user = userRepository.findById(userId)
                            .orElse(null);

                    if (user == null) {
                        return null;
                    }

                    UserContact contact = userContactRepository
                            .findByUserIdAndContactType(userId, "EMAIL")
                            .orElse(null);

                    String email = contact != null
                            ? contact.getContactValue()
                            : "";

                    return new AdminCustomerResponse(
                            user.getId(),
                            user.getFullName(),
                            email,
                            user.getAccountStatus(),
                            user.getCreatedAt(),
                            user.getLastLoginAt()
                    );
                })
                .filter(customer -> customer != null)
                .toList();
    }
}
