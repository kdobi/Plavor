package com.plavor.backend.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public List<ProductSummaryResponse> getProducts() {
		return productService.getProducts();
	}

	@GetMapping("/{id}")
	public ProductDetailResponse getProduct(@PathVariable Long id) {
		return productService.getProduct(id);
	}
}
