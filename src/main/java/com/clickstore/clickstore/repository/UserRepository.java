
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
