package com.sodimac.aclaraciones.api.controller;

import com.sodimac.aclaraciones.api.model.dto.feedback.FeedbackDto;
import com.sodimac.aclaraciones.api.security.Session;
import com.sodimac.aclaraciones.api.service.feedback.command.FeedbackCommandService;
import com.sodimac.aclaraciones.api.service.feedback.query.FeedbackQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    private MockMvc mvc;

    @Mock
    private FeedbackQueryService queryService;
    @Mock
    private FeedbackCommandService commandService;

    @BeforeEach
    void setup() {
        FeedbackController controller = new FeedbackController(queryService, commandService);
        this.mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("GET /feedback sin size: 200 y delega en queryService.list(null)")
    void list_default_ok() throws Exception {
        when(queryService.list(null)).thenReturn(List.of(Mockito.mock(FeedbackDto.class)));

        mvc.perform(
                get("/feedback")
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isOk());

        verify(queryService).list(isNull());
    }

    @Test
    @DisplayName("GET /feedback?size=20: 200 y delega en queryService.list(20)")
    void list_with_size_ok() throws Exception {
        when(queryService.list(20)).thenReturn(List.of(Mockito.mock(FeedbackDto.class)));

        mvc.perform(
                get("/feedback")
                        .param("size", "20")
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isOk());

        verify(queryService).list(20);
    }

    @Test
    @DisplayName("GET /feedback/{id}: 200 y delega en queryService.findById")
    void get_one_ok() throws Exception {
        when(queryService.findById(5L)).thenReturn(Mockito.mock(FeedbackDto.class));

        mvc.perform(
                get("/feedback/{id}", 5L)
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isOk());

        verify(queryService).findById(5L);
    }

    @Test
    @DisplayName("POST /feedback: 201 y delega en commandService.create")
    void create_created() throws Exception {
        when(commandService.create(any(), any())).thenReturn(Mockito.mock(FeedbackDto.class));

        mvc.perform(
                post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isCreated());

        verify(commandService).create(any(FeedbackDto.class), any(Session.class));
    }

    @Test
    @DisplayName("PUT /feedback/{id}: 200 y delega en commandService.update")
    void update_ok() throws Exception {
        when(commandService.update(eq(9L), any(), any())).thenReturn(Mockito.mock(FeedbackDto.class));

        mvc.perform(
                put("/feedback/{id}", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\"}")
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isOk());

        verify(commandService).update(eq(9L), any(FeedbackDto.class), any(Session.class));
    }

    @Test
    @DisplayName("POST /feedback/{id}/publish: 200 y delega en updatePublication(id, true)")
    void publish_ok() throws Exception {
        when(commandService.updatePublication(eq(7L), eq(true), any())).thenReturn(Mockito.mock(FeedbackDto.class));

        mvc.perform(
                post("/feedback/{id}/publish", 7L)
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isOk());

        verify(commandService).updatePublication(eq(7L), eq(true), any(Session.class));
    }

    @Test
    @DisplayName("POST /feedback/{id}/unpublish: 200 y delega en updatePublication(id, false)")
    void unpublish_ok() throws Exception {
        when(commandService.updatePublication(eq(7L), eq(false), any())).thenReturn(Mockito.mock(FeedbackDto.class));

        mvc.perform(
                post("/feedback/{id}/unpublish", 7L)
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isOk());

        verify(commandService).updatePublication(eq(7L), eq(false), any(Session.class));
    }

    @Test
    @DisplayName("DELETE /feedback/{id}: 204 y delega en commandService.delete")
    void delete_no_content() throws Exception {
        mvc.perform(
                delete("/feedback/{id}", 33L)
                        .requestAttr("session", Mockito.mock(Session.class)))
                .andExpect(status().isNoContent());

        verify(commandService).delete(33L);
    }
}
