package com.deb.wheat.vaadin.repository;

import com.deb.wheat.vaadin.data.entity.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductInfoRepository extends JpaRepository<ProductInfo, UUID> {
    ProductInfo findByProductIdentityInfo(String info);

    @Query("SELECT  p from ProductInfo p where p.productIdentityInfo like ?2")
    Page<ProductInfo> findAllByProduct(Pageable pageable, String filter);
}
