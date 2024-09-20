package pe.edu.vallegrande.vg_ms_income.presentation.controller.Category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_income.application.service.CategoryService;
import pe.edu.vallegrande.vg_ms_income.domain.model.Category;
import reactor.core.publisher.Flux;


@RestController
@RequiredArgsConstructor
@RequestMapping("/community/income_category/v1")
public class CategoryCommunityController {
    private final CategoryService categoryService;


    @GetMapping("/list")
    public Flux<Category> getAllAccountings() {
        return categoryService.getAll();
    }


}
