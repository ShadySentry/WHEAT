package ch.martinelli.demo.vaadin.views;

import org.springframework.context.ApplicationEvent;

public class SamplePersonAddedEvent extends ApplicationEvent {

    public SamplePersonAddedEvent(Object source) {
        super(source);
    }
}
