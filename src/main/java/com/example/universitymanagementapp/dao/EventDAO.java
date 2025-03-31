package com.example.universitymanagementapp.dao;

import com.example.universitymanagementapp.model.Event;

import java.util.List;
import java.util.ArrayList;

public class EventDAO {
    private static ArrayList<Event> events = new ArrayList<>();

    // Add event
    public void addEvent(Event event) {
        events.add(event);
        System.out.println("Event added successfully: " + event.getEventCode());
    }

    // Delete event by code
    public void deleteEvent(String eventCode) {
        events.removeIf(event -> event.getEventCode().equals(eventCode));
        System.out.println("Event deleted successfully: " + eventCode);
    }

    // Update event
    public void updateEvent(String oldCode, Event updatedEvent) {
        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            if (e.getEventCode().equals(oldCode)) {
                events.set(i, updatedEvent);
                System.out.println("Event updated successfully: " + oldCode + " -> " + updatedEvent.getEventCode());
                return;
            }
        }
        System.out.println("Event not found for update: " + oldCode);
    }

    // Find event by name
    public Event findEventByName(String eventName) {
        return events.stream()
                .filter(event -> event.getEventName().equalsIgnoreCase(eventName))
                .findFirst()
                .orElse(null);
    }

    // Get all events
    public List<Event> getAllEvents() {
        return new ArrayList<>(events); // Return a copy to prevent external modification
    }

    // Clear all events
    public void clearEvents() {
        events.clear();
        System.out.println("All events cleared");
    }

}
