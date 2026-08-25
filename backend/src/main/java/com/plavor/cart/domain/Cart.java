package com.plavor.cart.domain;

import com.plavor.catalog.domain.Product;
import com.plavor.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carts")
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private Member member;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<CartItem> items = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Cart() {
	}

	public Cart(Member member) {
		this.member = member;
	}

	public CartItem addItem(Product product, int quantity) {
		Optional<CartItem> existingItem = findItemByProductId(product.getId());
		if (existingItem.isPresent()) {
			existingItem.get().increaseQuantity(quantity);
			return existingItem.get();
		}

		CartItem item = new CartItem(this, product, quantity);
		items.add(item);
		return item;
	}

	public void removeItem(CartItem item) {
		items.remove(item);
	}

	public Optional<CartItem> findItemByProductId(Long productId) {
		return items.stream()
				.filter(item -> item.hasProduct(productId))
				.findFirst();
	}

	public Long getId() {
		return id;
	}

	public Member getMember() {
		return member;
	}

	public List<CartItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
