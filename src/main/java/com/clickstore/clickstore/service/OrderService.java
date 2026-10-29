
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.dto.CartItemResponse;
import com.clickstore.clickstore.entity.Address;
import com.clickstore.clickstore.entity.Cart;
import com.clickstore.clickstore.entity.CartItem;
import com.clickstore.clickstore.entity.Order;
import com.clickstore.clickstore.entity.OrderItem;
import com.clickstore.clickstore.entity.Product;
import com.clickstore.clickstore.entity.ProductVariant;
import com.clickstore.clickstore.entity.SellerOrder;
import com.clickstore.clickstore.repository.AddressRepository;
import com.clickstore.clickstore.repository.CartItemRepository;
import com.clickstore.clickstore.repository.CartRepository;
import com.clickstore.clickstore.repository.OrderItemRepository;
import com.clickstore.clickstore.repository.OrderRepository;
import com.clickstore.clickstore.repository.ProductRepository;
import com.clickstore.clickstore.repository.ProductVariantRepository;
import com.clickstore.clickstore.repository.SellerOrderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CartItemService cartItemService;

    public OrderService(
            OrderRepository orderRepository,
            SellerOrderRepository sellerOrderRepository,
            OrderItemRepository orderItemRepository,
            CartService cartService,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            AddressRepository addressRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            CartItemService cartItemService
    ) {
        this.orderRepository = orderRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.cartItemService = cartItemService;
    }

    @Transactional
    public Order placeOrder(
            Long customerId,
            Long addressId,
            List<Long> cartItemIds
    ) {
        if (customerId == null || customerId <= 0
                || addressId == null || addressId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid customer and address IDs are required"
            );
        }

        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select at least one cart item"
            );
        }

        if (cartItemIds.stream().anyMatch(
                id -> id == null || id <= 0
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid cart item IDs"
            );
        }

        Set<Long> selectedIds = new LinkedHashSet<>(cartItemIds);

        if (selectedIds.size() != cartItemIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate cart item IDs are not allowed"
            );
        }

        Address address = addressRepository
                .findByIdAndUserId(addressId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipping address not found"
                ));

        Cart cart = cartService.findActiveCart(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Your cart is empty"
                ));

        // Fetch only the selected rows belonging to this active cart.
        List<CartItem> selectedCartEntities =
                cartItemRepository.findByCartIdAndIdIn(
                        cart.getId(),
                        selectedIds
                );

        if (selectedCartEntities.size() != selectedIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more selected items are invalid or no longer in your cart"
            );
        }

        // Load cart details and retain only the selected items.
        List<CartItemResponse> allItems =
                cartItemService.getItems(customerId);

        if (allItems == null || allItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Your cart is empty"
            );
        }

        List<CartItemResponse> items = allItems.stream()
                .filter(item -> selectedIds.contains(item.cartItemId()))
                .collect(Collectors.toList());

        if (items.size() != selectedIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Could not validate all selected cart items"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Map<Long, List<CartItemResponse>> sellerItems =
                new LinkedHashMap<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        String currencyCode = null;

        // Validate selected products and calculate prices from the database.
        for (CartItemResponse item : items) {

            ProductVariant variant = variantRepository
                    .findById(item.variantId())
                    .filter(ProductVariant::isActive)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "A product variant is no longer available"
                    ));

            Product product = productRepository
                    .findById(variant.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "A product in your cart no longer exists"
                    ));

            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid cart quantity"
                );
            }

            if (variant.getPrice() == null
                    || variant.getCurrencyCode() == null
                    || variant.getCurrencyCode().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product price or currency is invalid"
                );
            }

            if (currencyCode == null) {
                currencyCode = variant.getCurrencyCode();
            } else if (!currencyCode.equalsIgnoreCase(
                    variant.getCurrencyCode())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Products with different currencies cannot be ordered together"
                );
            }

            BigDecimal lineTotal = variant.getPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()));

            subtotal = subtotal.add(lineTotal);

            sellerItems.computeIfAbsent(
                    product.getSellerId(),
                    key -> new ArrayList<>()
            ).add(item);
        }

        // Create the main customer order.
        Order order = new Order();

        order.setOrderNumber("ORD-" +
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase());

        order.setCustomerId(customerId);
        order.setShippingAddressId(address.getId());
        order.setRecipientName(address.getRecipientName());
        order.setShippingPhone(address.getPhone());

        order.setShippingAddressLine1(address.getAddressLine1());
        order.setShippingAddressLine2(address.getAddressLine2());
        order.setShippingLandmark(address.getLandmark());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingCountryCode(address.getCountryCode());

        order.setCurrencyCode(currencyCode);
        order.setSubtotal(subtotal);
        order.setDiscountTotal(BigDecimal.ZERO);
        order.setShippingTotal(BigDecimal.ZERO);
        order.setTaxTotal(BigDecimal.ZERO);
        order.setGrandTotal(subtotal);
        order.setStatus("PLACED");
        order.setPlacedAt(now);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        order = orderRepository.save(order);

        // Create one seller order per seller.
        for (Map.Entry<Long, List<CartItemResponse>> entry
                : sellerItems.entrySet()) {

            Long sellerId = entry.getKey();
            List<CartItemResponse> sellerCartItems = entry.getValue();

            BigDecimal sellerSubtotal = BigDecimal.ZERO;

            for (CartItemResponse item : sellerCartItems) {
                ProductVariant variant = variantRepository
                        .findById(item.variantId())
                        .filter(ProductVariant::isActive)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "A product variant is no longer available"
                        ));

                sellerSubtotal = sellerSubtotal.add(
                        variant.getPrice().multiply(
                                BigDecimal.valueOf(item.quantity())
                        )
                );
            }

            SellerOrder sellerOrder = new SellerOrder();

            sellerOrder.setOrderId(order.getId());
            sellerOrder.setSellerId(sellerId);
            sellerOrder.setSellerOrderNumber("SORD-" +
                    UUID.randomUUID().toString()
                            .replace("-", "")
                            .substring(0, 20)
                            .toUpperCase());

            sellerOrder.setStatus("PLACED");
            sellerOrder.setSubtotal(sellerSubtotal);
            sellerOrder.setDiscountTotal(BigDecimal.ZERO);
            sellerOrder.setShippingTotal(BigDecimal.ZERO);
            sellerOrder.setTaxTotal(BigDecimal.ZERO);
            sellerOrder.setGrandTotal(sellerSubtotal);
            sellerOrder.setPlacedAt(now);
            sellerOrder.setCreatedAt(now);
            sellerOrder.setUpdatedAt(now);

            sellerOrder = sellerOrderRepository.save(sellerOrder);

            // Save snapshots of the selected items only.
            for (CartItemResponse item : sellerCartItems) {

                ProductVariant variant = variantRepository
                        .findById(item.variantId())
                        .filter(ProductVariant::isActive)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "A product variant is no longer available"
                        ));

                Product product = productRepository
                        .findById(variant.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Product not found"
                        ));

                BigDecimal lineTotal = variant.getPrice()
                        .multiply(BigDecimal.valueOf(item.quantity()));

                OrderItem orderItem = new OrderItem();

                orderItem.setSellerOrderId(sellerOrder.getId());
                orderItem.setSellerId(sellerId);
                orderItem.setProductId(product.getId());
                orderItem.setVariantId(variant.getId());
                orderItem.setProductNameSnapshot(product.getName());
                orderItem.setSkuSnapshot(variant.getSku());
                orderItem.setVariantNameSnapshot(
                        variant.getVariantName()
                );
                orderItem.setUnitPrice(variant.getPrice());
                orderItem.setQuantity(item.quantity());
                orderItem.setItemSubtotal(lineTotal);
                orderItem.setItemDiscount(BigDecimal.ZERO);
                orderItem.setLineTotal(lineTotal);
                orderItem.setCreatedAt(now);

                orderItemRepository.save(orderItem);
            }
        }

        // Remove only the selected items from the cart.
        cartItemRepository.deleteAll(selectedCartEntities);

        // Keep the cart ACTIVE if unselected items remain.
        List<CartItem> remainingItems =
                cartItemRepository.findByCartId(cart.getId());

        cart.setUpdatedAt(now);

        if (remainingItems.isEmpty()) {
            cart.setStatus("CONVERTED");
        }

        cartRepository.save(cart);

        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(Long customerId) {
        return orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrdersForAdmin() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order status is required"
            );
        }

        String status = newStatus.trim().toUpperCase();

        List<String> allowedStatuses = List.of(
                "PLACED",
                "CONFIRMED",
                "SHIPPED",
                "DELIVERED",
                "CANCELLED"
        );

        if (!allowedStatuses.contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order status"
            );
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        if ("DELIVERED".equals(order.getStatus())
                || "CANCELLED".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Delivered or cancelled orders cannot be updated"
            );
        }

        order.setStatus(status);
        order.setUpdatedAt(OffsetDateTime.now());

        return orderRepository.save(order);
    }
}
