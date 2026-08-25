package com.plavor.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(name = "alt_text", length = 255)
	private String altText;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@Column(nullable = false)
	private boolean thumbnail;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected ProductImage() {
	}

	public ProductImage(Product product, String imageUrl, String altText, int displayOrder, boolean thumbnail) {
		this.product = product;
		this.imageUrl = imageUrl;
		this.altText = altText;
		this.displayOrder = displayOrder;
		this.thumbnail = thumbnail;
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

	public int getDisplayOrder() {
		return displayOrder;
	}

	public boolean isThumbnail() {
		return thumbnail;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
