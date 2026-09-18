package com.sodimac.fiscal.api.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.fiscal.api.config.FiscalAuditConfig;
import com.sodimac.fiscal.api.filter.FiscalAuditFilter;
import com.sodimac.fiscal.api.security.Session;
import com.sodimac.fiscal.api.service.impl.AuditoriaApiServiceImpl;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FiscalAuditPatchTest {
    private AuditoriaApiServiceImpl audit;
    private MockMvc mvc;
    @BeforeEach void setup() {
        audit = mock(AuditoriaApiServiceImpl.class);
        when(audit.isEnabled()).thenReturn(true);
        var config = new FiscalAuditConfig(audit);
        mvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(config).addInterceptors(config)
                .addFilters(new FiscalAuditFilter(audit)).build();
    }
    @AfterEach void resetContext() { RequestContextHolder.resetRequestAttributes(); }

    @RestController static class ProbeController {
        @GetMapping("/ok") Map<String,Object> ok() { return Map.of("success",true); }
        @GetMapping("/business") Map<String,Object> business() {
            return Map.of("success",true,"isValid",false,"code","BUS042");
        }
        @GetMapping("/failed") ResponseEntity<?> failed() { return ResponseEntity.status(422).body(Map.of("errorCode","BUS028")); }
        @GetMapping("/pdf") ResponseEntity<byte[]> pdf() {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(new byte[]{0,1,2,3,-1});
        }
        @PostMapping("/echo") Map<String,Object> echo(@RequestBody Map<String,Object> body) { return body; }
    }
    @Test void successHasOneStartInitAndEndAndTrace() throws Exception {
        mvc.perform(get("/ok")).andExpect(status().isOk()).andExpect(header().exists("X-Trace-Id"));
        verify(audit).httpEvent(any(),eq("FiscalApi"),any(),eq("START_GET"),eq(false),eq(0L),anyMap(),isNull(),isNull());
        verify(audit).httpEvent(any(),eq("ProbeController"),eq("ok"),eq("INIT_METHOD"),eq(false),eq(0L),anyMap(),isNull(),isNull());
        verify(audit).httpEvent(any(),eq("ProbeController"),eq("ok"),eq("END_GET"),eq(false),anyLong(),anyMap(),isNull(),isNull());
    }
    @Test void http200WithInvalidDocumentIsError() throws Exception {
        mvc.perform(get("/business").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.isValid").value(false));
        verify(audit).httpEvent(any(),any(),eq("business"),eq("END_GET"),eq(true),anyLong(),anyMap(),eq("BUS042"),isNull());
    }
    @Test void returnedHttpErrorIsError() throws Exception {
        mvc.perform(get("/failed")).andExpect(status().isUnprocessableEntity());
        verify(audit).httpEvent(any(),any(),any(),eq("END_GET"),eq(true),anyLong(),anyMap(),eq("BUS028"),isNull());
    }
    @Test void binaryDownloadIsUnchanged() throws Exception {
        mvc.perform(get("/pdf")).andExpect(content().bytes(new byte[]{0,1,2,3,-1}));
        verify(audit).httpEvent(any(),any(),eq("pdf"),eq("END_GET"),eq(false),anyLong(),anyMap(),isNull(),isNull());
    }
    @Test void requestBodyIsNotConsumed() throws Exception {
        mvc.perform(post("/echo").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("{\"value\":42}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.value").value(42));
    }
    @Test void rejectionBeforeControllerStillClosesAsError() throws Exception {
        new FiscalAuditFilter(audit).doFilter(new MockHttpServletRequest("GET","/private"),new MockHttpServletResponse(),
                (req,res) -> ((jakarta.servlet.http.HttpServletResponse)res).setStatus(401));
        verify(audit).httpEvent(any(),any(),any(),eq("END_GET"),eq(true),anyLong(),anyMap(),eq("401"),isNull());
    }
    @Test void unhandledExceptionIsRethrownAndAudited() {
        assertThrows(ServletException.class, () -> new FiscalAuditFilter(audit).doFilter(
                new MockHttpServletRequest("GET","/boom"),new MockHttpServletResponse(),
                (req,res) -> { throw new ServletException("failure"); }));
        verify(audit).httpEvent(any(),any(),any(),eq("END_GET"),eq(true),anyLong(),anyMap(),eq("UNHANDLED_ERROR"),contains("ServletException"));
    }
    @Test void asyncClosesOnlyAfterCompletion() throws Exception {
        var request = new MockHttpServletRequest("GET", "/async");
        var response = new MockHttpServletResponse();
        request.setAsyncSupported(true);
        new FiscalAuditFilter(audit).doFilter(request,response,(req,res) -> req.startAsync(req,res));
        verify(audit,never()).httpEvent(any(),any(),any(),eq("END_GET"),anyBoolean(),anyLong(),anyMap(),any(),any());
        request.getAsyncContext().complete();
        verify(audit,times(1)).httpEvent(any(),any(),any(),eq("END_GET"),eq(false),anyLong(),anyMap(),isNull(),isNull());
    }
    @Test void asyncTimeoutIsError() throws Exception {
        var request = new MockHttpServletRequest("GET", "/async");
        var response = new MockHttpServletResponse();
        request.setAsyncSupported(true);
        new FiscalAuditFilter(audit).doFilter(request,response,(req,res) -> req.startAsync(req,res));
        var context = (org.springframework.mock.web.MockAsyncContext)request.getAsyncContext();
        for (var listener : context.getListeners()) listener.onTimeout(new jakarta.servlet.AsyncEvent(context));
        context.complete();
        verify(audit).httpEvent(any(),any(),any(),eq("END_GET"),eq(true),anyLong(),anyMap(),eq("ASYNC_TIMEOUT"),isNull());
    }
    @Test void exceptionObserverDoesNotHandleOrReplaceException() {
        List<HandlerExceptionResolver> resolvers = new ArrayList<>();
        new FiscalAuditConfig(audit).extendHandlerExceptionResolvers(resolvers);
        var req = new MockHttpServletRequest();
        var exception = new IllegalArgumentException("invalid");
        assertNull(resolvers.get(0).resolveException(req,new MockHttpServletResponse(),null,exception));
        assertSame(exception, req.getAttribute(AuditoriaApiServiceImpl.ERROR));
    }
    @Test void jdbcPersistsSameTraceAndVerifiedUserUsingSeparateConnection() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection business = mock(Connection.class), independent = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(ds.getConnection()).thenReturn(independent);
        when(independent.prepareStatement(anyString())).thenReturn(statement);
        var req = new MockHttpServletRequest();
        String trace = UUID.randomUUID().toString();
        req.setAttribute(AuditoriaApiServiceImpl.TRACE,trace);
        req.setAttribute("session",new Session("test","verified@example.test",List.of()));
        req.addHeader("TraceFrontId","frontend-non-uuid");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        TransactionSynchronizationManager.bindResource(ds,new ConnectionHolder(business));
        try {
            new AuditoriaApiServiceImpl(ds,new ObjectMapper(),true).logActivity("business-id","VALIDAR_SAT",
                    "InvoiceService","system",true,"Rejected","detail",Map.of("errorCode","BUS001"),5L);
            verify(independent).setAutoCommit(true);
            verify(statement).setObject(eq(1),eq(trace),anyInt());
            verify(statement).setString(5,"API_FISCAL");
            verify(statement).setString(10,"verified@example.test");
            verify(statement).setString(13,"ERROR");
            verify(statement).setString(14,"BUS001");
            ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
            verify(statement).setString(eq(12),json.capture());
            assertEquals("frontend-non-uuid",new ObjectMapper().readTree(json.getValue()).get("trace_front_id_original").asText());
            verify(statement).executeUpdate();
            verify(independent).close();
            verifyNoInteractions(business);
        } finally { TransactionSynchronizationManager.unbindResource(ds); }
    }
    @Test void auditDatabaseFailureDoesNotPropagate() throws Exception {
        DataSource ds = mock(DataSource.class);
        when(ds.getConnection()).thenThrow(new SQLException("unavailable"));
        var service = new AuditoriaApiServiceImpl(ds,new ObjectMapper(),true);
        assertDoesNotThrow(() -> service.logActivity(null,"TEST","Fiscal","system",false,"test",null,null,null));
    }
    @Test void disabledAuditDoesNotAcquireConnection() {
        DataSource ds = mock(DataSource.class);
        new AuditoriaApiServiceImpl(ds,new ObjectMapper(),false).logActivity(null,"TEST","Fiscal",null,false,"test",null,null,null);
        verifyNoInteractions(ds);
    }
}
