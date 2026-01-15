package com.example.airline.controller.admin;

import com.example.airline.dto.tour.TourCategoryDto;
import com.example.airline.service.tour.TourCategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
public class TourCategoryController {
    
    private final TourCategoryService categoryService;
    
    public TourCategoryController(TourCategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @PostMapping
    public ResponseEntity<TourCategoryDto> createCategory(@Valid @RequestBody TourCategoryDto dto) {
        try {
            TourCategoryDto created = categoryService.createCategory(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TourCategoryDto>> getAllCategories() {
        List<TourCategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/page")
    public ResponseEntity<Page<TourCategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<TourCategoryDto> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TourCategoryDto> getCategoryById(@PathVariable Long id) {
        try {
            TourCategoryDto category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TourCategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody TourCategoryDto dto) {
        try {
            TourCategoryDto updated = categoryService.updateCategory(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

