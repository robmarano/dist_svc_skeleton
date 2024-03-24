package edu.cooper.ece465.zk.listeners;

import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;

public class SpringAppEventsListener implements ApplicationListener<SpringApplicationEvent> {
    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        System.out.println("SpringAppEventsListener.onApplicationEvent");
        // event handling
        System.out.println( " > " + event );
    }
}