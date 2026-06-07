package com.emailagent.agent;

import com.emailagent.model.EmailRequest;
import com.emailagent.model.EmailResponse;
import com.emailagent.model.GeneratedEmailContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOrchestratorAgentTest {
//
//    @Mock
//    private LlmEmailGeneratorAgent llmAgent;
//
//    @Mock
//    private EmailSenderAgent senderAgent;
//
//    @InjectMocks
//    private EmailOrchestratorAgent orchestrator;
//
//    private EmailRequest buildRequest() {
//        return EmailRequest.builder()
//                .toEmail("lala@gmail.com")
//                .description("Draft a congratulation email to employee over achievement")
//                .topic("Congratulation email to employee")
//                .myNumber("6878367864")
//                .myName("Kuber")
//                .toName("Lala")
//                .build();
//    }
//
//    @Test
//    void shouldReturnSuccessWhenEmailSentSuccessfully() {
//        EmailRequest request = buildRequest();
//
//        GeneratedEmailContent content = GeneratedEmailContent.builder()
//                .subject("Congratulations, Lala!")
//                .body("Dear Lala,\n\nWell done on your achievement!\n\nBest,\nKuber")
//                .senderName("Kuber")
//                .recipientName("Lala")
//                .build();
//
//        when(llmAgent.generateEmail(any())).thenReturn(content);
//        doNothing().when(senderAgent).sendEmail(eq("lala@gmail.com"), any());
//
//        EmailResponse response = orchestrator.process(request);
//
//        assertThat(response.isSuccess()).isTrue();
//        assertThat(response.getGeneratedSubject()).isEqualTo("Congratulations, Lala!");
//        assertThat(response.getSentTo()).isEqualTo("lala@gmail.com");
//
//        verify(llmAgent, times(1)).generateEmail(request);
//        verify(senderAgent, times(1)).sendEmail(eq("lala@gmail.com"), eq(content));
//    }
//
//    @Test
//    void shouldReturnFailureWhenLlmAgentThrowsException() {
//        EmailRequest request = buildRequest();
//        when(llmAgent.generateEmail(any())).thenThrow(new RuntimeException("LLM API error"));
//
//        EmailResponse response = orchestrator.process(request);
//
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getError()).contains("LLM API error");
//
//        verify(senderAgent, never()).sendEmail(any(), any());
//    }
//
//    @Test
//    void shouldReturnFailureWhenSenderAgentThrowsException() {
//        EmailRequest request = buildRequest();
//
//        GeneratedEmailContent content = GeneratedEmailContent.builder()
//                .subject("Congratulations!")
//                .body("Dear Lala, well done!")
//                .build();
//
//        when(llmAgent.generateEmail(any())).thenReturn(content);
//        doThrow(new RuntimeException("SMTP connection failed"))
//                .when(senderAgent).sendEmail(any(), any());
//
//        EmailResponse response = orchestrator.process(request);
//
//        assertThat(response.isSuccess()).isFalse();
//        assertThat(response.getError()).contains("SMTP connection failed");
//    }
}
