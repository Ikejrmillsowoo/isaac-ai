public record ChatRequest(

        @NotNull
        UUID workspaceId,

        @NotNull
        UUID conversationId,

        @NotBlank
        String message

) {
}