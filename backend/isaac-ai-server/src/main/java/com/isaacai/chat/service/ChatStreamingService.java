// package com.isaacai.chat.service;

// import com.isaacai.ai.client.AiStreamingClient;
// import com.isaacai.server.message.model.Message;    


// public Flux<String> stream(ChatRequest request) {

//     Message userMessage = ...

//     List<Message> history = ...

//     StringBuilder builder = new StringBuilder();

//     return aiStreamingClient.stream(history)
//             .doOnNext(builder::append)
//             .doOnComplete(() ->
//                     messageService.createAssistantMessage(
//                             request.workspaceId(),
//                             request.conversationId(),
//                             builder.toString()
//                     )
//             );
// }