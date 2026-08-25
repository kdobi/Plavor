package com.plavor.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false)
	private long price;

	@Column(name = "stock_quantity", nullable = false)
	private int stockQuantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductStatus status;

	@OneToMany(mappedBy = "product")
	@OrderBy("displayOrder ASC, id ASC")
	private List<ProductImage> images = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Product() {
	}

	public Long getId() {
		return id;
	}

	public Category getCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public long getPrice() {
		return price;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public ProductStatus getStatus() {
		return status;
	}

	public boolean hasStock(int quantity) {
		return stockQuantity >= quantity;
	}

	public void decreaseStock(int quantity) {
		if (quantity < 1 || stockQuantity < quantity) {
			throw new IllegalArgumentException("재고보다 많은 수량을 차감할 수 없습니다.");
		}

		stockQuantity -= quantity;

		if (stockQuantity == 0 && status == ProductStatus.ACTIVE) {
			status = ProductStatus.SOLD_OUT;
		}
	}

	public List<ProductImage> getImages() {
		return Collections.unmodifiableList(images);
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
