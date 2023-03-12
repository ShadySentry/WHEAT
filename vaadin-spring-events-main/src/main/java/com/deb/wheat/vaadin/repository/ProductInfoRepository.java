package com.deb.wheat.vaadin.repository;

import com.deb.wheat.vaadin.data.entity.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductInfoRepository extends JpaRepository<ProductInfo, UUID> {
    ProductInfo findByProductIdentityInfo(String info);
}
