package ch.martinelli.demo.vaadin.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ProductInfo extends AbstractEntity{

    @Column(unique = true)
    private String productIdentityInfo;

    private String description;

    @OneToMany
    List<OperatorToDoItems> todos;
}
