package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndActiveTrue(Long id);

    List<Category> findByActiveTrueOrderByNameAsc();
}