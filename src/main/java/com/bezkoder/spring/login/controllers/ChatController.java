package com.bezkoder.spring.login.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.bezkoder.spring.login.models.ChatMessage;
import com.bezkoder.spring.login.models.User;
import com.bezkoder.spring.login.repository.UserRepository;

import java.util.Optional;

@Controller
public class ChatController {

    private final UserRepository userRepository;

    @Autowired
    public ChatController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @MessageMapping("/api/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        Optional<User> sender = userRepository.findByUsername(message.getSender().getUsername());
        if (sender.isPresent()) {
            message.setSender(sender.get());
        }
        
        // Handle incoming message and broadcast it to all subscribers
        return message;
    }
}
