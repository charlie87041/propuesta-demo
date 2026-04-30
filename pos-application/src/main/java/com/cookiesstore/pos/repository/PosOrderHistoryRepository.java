package com.cookiesstore.pos.repository;

import com.cookiesstore.common.entities.AdminSourcePosOrder;
import com.cookiesstore.common.entities.Order;
import com.cookiesstore.common.entities.OrderItem;
import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.common.entities.Source;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PosOrderHistoryRepository extends JpaRepository<Order, Long> {

    @Query("""
        select o
        from Order o
        where exists (
            select 1 from AdminSourcePosOrder posOrder
            where posOrder.order = o and posOrder.source.id = :sourceId
        )
        and (:status is null or o.status = :status)
        and (
            :search is null
            or cast(o.incrementId as string) ilike concat('%', cast(:search as string), '%')
            or cast(coalesce(o.customerFirstName, '') as string) ilike concat('%', cast(:search as string), '%')
            or cast(coalesce(o.customerLastName, '') as string) ilike concat('%', cast(:search as string), '%')
            or cast(coalesce(o.customerEmail, '') as string) ilike concat('%', cast(:search as string), '%')
        )
        order by o.createdAt desc
    """)
    Page<Order> findHistoryPage(
        @Param("sourceId") Long sourceId,
        @Param("status") OrderStatus status,
        @Param("search") String search,
        Pageable pageable
    );

     @Query("""
        select p
        from AdminSourcePosOrder p
        where p.order.id = :orderId
    """)
    Optional<AdminSourcePosOrder> findOrderPosSource(
        @Param("orderId") Long orderId
    );

    @Query("""
        select count(o)
        from Order o
        where exists (
            select 1 from AdminSourcePosOrder posOrder
            where posOrder.order = o and posOrder.source.id = :sourceId
        )
    """)
    long countAllBySource(@Param("sourceId") Long sourceId);

    @Query("""
        select coalesce(sum(o.grandTotalMinor), 0)
        from Order o
        where exists (
            select 1 from AdminSourcePosOrder posOrder
            where posOrder.order = o and posOrder.source.id = :sourceId
        )
    """)
    long sumGrandTotalBySource(@Param("sourceId") Long sourceId);

    @Query("""
        select coalesce(sum(o.totalQtyOrdered), 0)
        from Order o
        where exists (
            select 1 from AdminSourcePosOrder posOrder
            where posOrder.order = o and posOrder.source.id = :sourceId
        )
    """)
    long sumTotalQtyBySource(@Param("sourceId") Long sourceId);

    @Query("""
        select coalesce(avg(o.grandTotalMinor), 0)
        from Order o
        where exists (
            select 1 from AdminSourcePosOrder posOrder
            where posOrder.order = o and posOrder.source.id = :sourceId
        )
    """)
    Double avgGrandTotalBySource(@Param("sourceId") Long sourceId);

    @Query("""
        select o
        from Order o
        where o.id = :orderId
        and exists (
            select 1 from AdminSourcePosOrder posOrder
            where posOrder.order = o and posOrder.source.id = :sourceId
        )
    """)
    Optional<Order> findByIdAndSourceId(@Param("orderId") Long orderId, @Param("sourceId") Long sourceId);

    @Query("""
        select oi
        from OrderItem oi
        where oi.order.id = :orderId
        and oi.source.id = :sourceId
        order by oi.id asc
    """)
    List<OrderItem> findItemsByOrderIdAndSourceId(@Param("orderId") Long orderId, @Param("sourceId") Long sourceId);
}
