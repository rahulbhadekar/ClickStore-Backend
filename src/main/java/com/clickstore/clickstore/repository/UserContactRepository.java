
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserContactRepository
        extends JpaRepository<UserContact, Long> {

    Optional<UserContact> findByContactTypeAndContactValue(
            String contactType,
            String contactValue
    );

    List<UserContact> findByUserId(Long userId);

    Optional<UserContact> findByUserIdAndContactType(
            Long userId,
            String contactType
    );
}
