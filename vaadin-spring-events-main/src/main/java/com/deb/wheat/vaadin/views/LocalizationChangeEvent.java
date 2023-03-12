package com.deb.wheat.vaadin.views;

import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class LocalizationChangeEvent extends ApplicationEvent {

    public LocalizationChangeEvent(Object source) {
        super(source);
    }
}
