package com.plavor.cart.service;

import com.plavor.cart.domain.Cart;
import com.plavor.cart.domain.CartItem;
import com.plavor.cart.dto.CartItemAddRequest;
import com.plavor.cart.dto.CartItemUpdateRequest;
import com.plavor.cart.dto.CartResponse;
import com.plavor.cart.repository.CartItemRepository;
import com.plavor.cart.repository.CartRepository;
import com.plavor.catalog.domain.Product;
import com.plavor.catalog.domain.ProductStatus;
import com.plavor.catalog.repository.ProductRepository;
import com.plavor.global.error.BusinessException;
import com.plavor.global.error.ErrorCode;
import com.plavor.member.domain.Member;
import com.plavor.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final MemberRepository memberRepository;

	public CartService(
			CartRepository cartRepository,
			CartItemRepository cartItemRepository,
			ProductRepository productRepository,
			MemberRepository memberRepository
	) {
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.productRepository = productRepository;
		this.memberRepository = memberRepository;
	}

	public CartResponse getCart(Long memberId) {
		return CartResponse.from(getOrCreateCart(memberId));
	}

	public CartResponse addItem(Long memberId, CartItemAddRequest request) {
		Cart cart = getOrCreateCart(memberId);
		Product product = getAvailableProduct(request.productId());
		int targetQuantity = cart.findItemByProductId(product.getId())
				.map(item -> item.getQuantity() + request.quantity())
				.orElse(request.quantity());

		validateStock(product, targetQuantity);
		cart.addItem(product, request.quantity());
		cartRepository.flush();

		return CartResponse.from(cart);
	}

	public CartResponse updateItem(Long memberId, Long itemId, CartItemUpdateRequest request) {
		CartItem item = getCartItem(memberId, itemId);
		validateStock(item.getProduct(), request.quantity());
		item.changeQuantity(request.quantity());
		cartRepository.flush();

		return CartResponse.from(item.getCart());
	}

	public void deleteItem(Long memberId, Long itemId) {
		CartItem item = getCartItem(memberId, itemId);
		item.getCart().removeItem(item);
		cartRepository.flush();
	}

	private Cart getOrCreateCart(Long memberId) {
		return cartRepository.findByMemberIdWithItems(memberId)
				.orElseGet(() -> createCart(memberId));
	}

	private Cart createCart(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "회원을 찾을 수 없습니다."));

		Cart cart = cartRepository.save(new Cart(member));
		cartRepository.flush();
		return cart;
	}

	private CartItem getCartItem(Long memberId, Long itemId) {
		return cartItemRepository.findByIdAndMemberId(itemId, memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
	}

	private Product getAvailableProduct(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_PRODUCT_NOT_AVAILABLE));

		if (product.getStatus() != ProductStatus.ACTIVE || !product.getCategory().isActive()) {
			throw new BusinessException(ErrorCode.CART_PRODUCT_NOT_AVAILABLE);
		}

		return product;
	}

	private void validateStock(Product product, int quantity) {
		if (quantity > product.getStockQuantity()) {
			throw new BusinessException(ErrorCode.CART_QUANTITY_EXCEEDS_STOCK);
		}
	}
}
