package com.isaacai.chat.service;

import com.isaacai.server.message.model.Message;

import java.util.List;

public record PreparedChat(


        Message userMessage,

        List<Message> history

) {
}