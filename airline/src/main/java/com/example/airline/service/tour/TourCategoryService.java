package com.example.airline.service.tour;

import com.example.airline.dto.tour.TourCategoryDto;
import com.example.airline.entity.tour.Tour;
import com.example.airline.entity.tour.TourCategory;
import com.example.airline.mapper.tour.TourCategoryMapper;
import com.example.airline.repository.tour.TourCategoryRepository;
import com.example.airline.repository.tour.TourRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TourCategoryService {
    
    private final TourCategoryRepository categoryRepository;
    private final TourRepository tourRepository;
    
    public TourCategoryService(TourCategoryRepository categoryRepository, TourRepository tourRepository) {
        this.categoryRepository = categoryRepository;
        this.tourRepository = tourRepository;
    }
    
    @Transactional
    public TourCategoryDto createCategory(TourCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists");
        }
        
        TourCategory category = TourCategoryMapper.toEntity(dto);
        category = categoryRepository.save(category);
        
        // Связывание с турами, если указаны
        if (dto.getTourIds() != null && !dto.getTourIds().isEmpty()) {
            Set<Tour> tours = new HashSet<>(tourRepository.findAllById(dto.getTourIds()));
            category.setTours(tours);
            category = categoryRepository.save(category);
        }
        
        return TourCategoryMapper.toDto(category);
    }
    
    @Transactional(readOnly = true)
    public List<TourCategoryDto> getAllCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(TourCategoryMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<TourCategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findByActiveTrue(pageable)
                .map(TourCategoryMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public TourCategoryDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(TourCategoryMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }
    
    @Transactional
    public TourCategoryDto updateCategory(Long id, TourCategoryDto dto) {
        TourCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        
        // Проверка уникальности имени (если изменилось)
        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists");
        }
        
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        if (dto.getActive() != null) {
            category.setActive(dto.getActive());
        }
        
        // Обновление связей с турами
        if (dto.getTourIds() != null) {
            Set<Tour> tours = new HashSet<>(tourRepository.findAllById(dto.getTourIds()));
            category.setTours(tours);
        }
        
        category = categoryRepository.save(category);
        return TourCategoryMapper.toDto(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        TourCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        
        // Удаляем связи с турами
        category.getTours().clear();
        categoryRepository.save(category);
        
        categoryRepository.delete(category);
    }
}

