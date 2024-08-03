package pe.edu.vallegrande.vg_ms_income.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_income.domain.model.Category;
import pe.edu.vallegrande.vg_ms_income.domain.repository.CategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Flux<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Mono<Category> getById(String categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Mono<Category> createCategory(Category category) {
        return categoryRepository.save(category);
    }
}
