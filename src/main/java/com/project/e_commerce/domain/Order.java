package com.project.e_commerce.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {
    @Setter(AccessLevel.NONE)
    private int orderId;
    private int userId;
    private List<OrderLine> items;
    private LocalDateTime orderDate;
    private LocalDate deliveryDate;
    private BigDecimal orderTotal;
    private OrderState orderState;

    public static boolean validateHasLines(List<OrderLine> orderLines){
        if(orderLines == null) return false;
        if(orderLines.isEmpty()) return false;
        return true;
    }

    public void computeTotal(){
        BigDecimal result = new BigDecimal(0);
        for (OrderLine orderLine: this.items ) {
            result = result.add(orderLine.getProductUnitPrice().multiply(BigDecimal.valueOf(orderLine.getQuantity())));
        }
        this.orderTotal = result;
    }

    public static LocalDate computeDeliveryDate(LocalDateTime orderDate){
        LocalDate result = LocalDate.from(orderDate.plusDays(3));
        if (result.getDayOfWeek().getValue() == DayOfWeek.SATURDAY.getValue()){
            result = result.plusDays(2);
        }
        if (result.getDayOfWeek().getValue() == DayOfWeek.SUNDAY.getValue()){
            result = result.plusDays(1);
        }
        return result;
    }

    public boolean isCancellable(LocalDateTime now){
        if(this.orderState == OrderState.CANCELLED) return false;
        if(this.orderState == OrderState.CONFIRMED && (now.isAfter(this.orderDate.plusHours(1) ) || now.isEqual(this.orderDate.plusHours(1)))){
            return false;
        }
        return true;
    }

    public static List<OrderLine> mergeLines(List<OrderLine> orderLines) {
        Map<Integer, OrderLine> byProductId = new LinkedHashMap<>();
        for (OrderLine line : orderLines) {
            byProductId.merge(line.getProductId(), line, (existing, incoming) -> {
                existing.setQuantity(existing.getQuantity() + incoming.getQuantity());
                return existing;
            });
        }
        return new ArrayList<>(byProductId.values());
    }
}
