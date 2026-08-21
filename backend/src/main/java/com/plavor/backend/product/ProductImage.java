package com.plavor.backend.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(name = "alt_text", length = 255)
	private String altText;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;

	@Column(nullable = false)
	private Boolean thumbnail;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private LocalDateTime createdAt;

	protected ProductImage() {
	}

	public Long getId() {
		return id;
	}

	public Product getProduct() {
		return product;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getAltText() {
		return altText;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public Boolean getThumbnail() {
		return thumbnail;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
