package ch.martinelli.demo.vaadin.views;

import org.springframework.context.ApplicationEvent;

public class ProductInfoAddedEvent extends ApplicationEvent {
    public ProductInfoAddedEvent(Object source) {
        super(source);
    }
}
