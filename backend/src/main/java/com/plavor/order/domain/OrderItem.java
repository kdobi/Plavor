package com.plavor.order.domain;

import com.plavor.catalog.domain.Product;
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
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "product_name", nullable = false, length = 200)
	private String productName;

	@Column(name = "unit_price", nullable = false)
	private long unitPrice;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "total_price", nullable = false)
	private long totalPrice;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected OrderItem() {
	}

	OrderItem(Order order, Product product, int quantity) {
		this.order = order;
		this.product = product;
		this.productName = product.getName();
		this.unitPrice = product.getPrice();
		this.quantity = quantity;
		this.totalPrice = unitPrice * quantity;
	}

	public Long getId() {
		return id;
	}

	public Order getOrder() {
		return order;
	}

	public Product getProduct() {
		return product;
	}

	public String getProductName() {
		return productName;
	}

	public long getUnitPrice() {
		return unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public long getTotalPrice() {
		return totalPrice;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
