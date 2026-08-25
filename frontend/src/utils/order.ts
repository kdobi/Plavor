import type { Order, OrderStatus } from '../types/order'

export function formatOrderDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export function formatOrderStatus(status: OrderStatus) {
  switch (status) {
    case 'CREATED':
      return '주문 접수'
    case 'PAID':
      return '결제 완료'
    case 'CANCELED':
      return '주문 취소'
  }
}

export function formatOrderTitle(order: Order) {
  const firstItem = order.items[0]

  if (!firstItem) {
    return '주문 상품'
  }

  if (order.items.length === 1) {
    return firstItem.productName
  }

  return `${firstItem.productName} 외 ${order.items.length - 1}건`
}

export function sumOrderQuantity(order: Order) {
  return order.items.reduce((total, item) => total + item.quantity, 0)
}
