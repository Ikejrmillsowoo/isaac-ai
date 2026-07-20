public record ChatResponse(

        UUID conversationId,

        UUID userMessageId,

        UUID assistantMessageId,

        String answer

) {
}