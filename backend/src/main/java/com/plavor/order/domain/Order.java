package com.plavor.order.domain;

import com.plavor.catalog.domain.Product;
import com.plavor.member.domain.Member;
import jakarta.persistence.CascadeType;
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
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Member member;

	@Column(name = "order_number", nullable = false, unique = true, length = 50)
	private String orderNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(name = "total_amount", nullable = false)
	private long totalAmount;

	@Column(name = "receiver_name", nullable = false, length = 100)
	private String receiverName;

	@Column(name = "receiver_phone", nullable = false, length = 30)
	private String receiverPhone;

	@Column(name = "postal_code", nullable = false, length = 20)
	private String postalCode;

	@Column(nullable = false, length = 255)
	private String address;

	@Column(name = "address_detail", length = 255)
	private String addressDetail;

	@Column(name = "delivery_message", length = 255)
	private String deliveryMessage;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<OrderItem> items = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "ordered_at", nullable = false, updatable = false)
	private LocalDateTime orderedAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected Order() {
	}

	public Order(
			Member member,
			String orderNumber,
			String receiverName,
			String receiverPhone,
			String postalCode,
			String address,
			String addressDetail,
			String deliveryMessage
	) {
		this.member = member;
		this.orderNumber = orderNumber;
		this.status = OrderStatus.CREATED;
		this.receiverName = receiverName;
		this.receiverPhone = receiverPhone;
		this.postalCode = postalCode;
		this.address = address;
		this.addressDetail = addressDetail;
		this.deliveryMessage = deliveryMessage;
	}

	public void addItem(Product product, int quantity) {
		OrderItem item = new OrderItem(this, product, quantity);

		items.add(item);
		totalAmount += item.getTotalPrice();
	}

	public Long getId() {
		return id;
	}

	public Member getMember() {
		return member;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void updateStatus(OrderStatus status) {
		this.status = status;
	}

	public long getTotalAmount() {
		return totalAmount;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getAddress() {
		return address;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public String getDeliveryMessage() {
		return deliveryMessage;
	}

	public List<OrderItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public LocalDateTime getOrderedAt() {
		return orderedAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
