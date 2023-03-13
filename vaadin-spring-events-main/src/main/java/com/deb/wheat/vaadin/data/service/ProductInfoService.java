package com.deb.wheat.vaadin.data.service;

import com.deb.wheat.vaadin.data.entity.ProductInfo;
import com.deb.wheat.vaadin.repository.ProductInfoRepository;
import com.deb.wheat.vaadin.views.ProductInfoAddedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProductInfoService {
    private  final ProductInfoRepository productInfoRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ProductInfoService(ProductInfoRepository productInfoRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.productInfoRepository = productInfoRepository;
        this.applicationEventPublisher=applicationEventPublisher;
    }


    public void createProductInfo(ProductInfo productInfo){
        if(productInfoRepository.findByProductIdentityInfo(productInfo.getProductIdentityInfo())!=null){
            log.warn("Cant create aProductInfo. Product already exists "+ productInfo);
            return;
        }

        productInfoRepository.save(productInfo);
        applicationEventPublisher.publishEvent(new ProductInfoAddedEvent(this));
    }

    public ProductInfo findProductByProductIdentityInfo(String productIdentityInfo){
        return productInfoRepository.findByProductIdentityInfo(productIdentityInfo);
    }

    public List<ProductInfo> findAll() {
        return productInfoRepository.findAll();
    }

    public Optional <ProductInfo> findById(UUID uuid){
        return productInfoRepository.findById(uuid);
    }

    public Page<ProductInfo> list(Pageable pageable) {
        return productInfoRepository.findAll(pageable);
    }

//    public List<ProductInfo> findAllByProductIdentityInfo(String value) {
//        if(value==null){
//            return productInfoRepository.
//        }else {
//            return productInfoRepository.findAll();
//        }
//    }
}
