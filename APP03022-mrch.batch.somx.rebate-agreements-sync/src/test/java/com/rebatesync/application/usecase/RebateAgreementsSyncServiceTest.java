package com.rebatesync.application.usecase;

import com.rebatesync.domain.enums.ContractStatus;
import com.rebatesync.domain.enums.SyncStatus;
import com.rebatesync.domain.exception.SyncException;
import com.rebatesync.domain.model.Agreement;
import com.rebatesync.domain.model.Contract;
import com.rebatesync.domain.model.RebateAgreement;
import com.rebatesync.domain.model.SyncResult;
import com.rebatesync.domain.port.output.ExternalRebateClient;
import com.rebatesync.domain.port.output.NotificationService;
import com.rebatesync.domain.port.output.RebateAgreementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RebateAgreementsSyncServiceTest {

    @Mock
    private ExternalRebateClient externalRebateClient;

    @Mock
    private RebateAgreementRepository rebateAgreementRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RebateAgreementsSyncService syncService;

    private Contract mockContract;
    private Agreement mockAgreement;
    private SyncResult mockSyncResult;

    @BeforeEach
    void setUp() {
        mockContract = Contract.builder()
                .contractId("1527")
                .vendorTaxId("123456789")
                .vendorName("Test Vendor")
                .status(ContractStatus.ACTIVE)
                .productCategoryId("CAT001")
                .agreementIds(Arrays.asList("AGR001", "AGR002"))
                .build();

        mockAgreement = Agreement.builder()
                .agreementId("AGR001")
                .productTypeSelector("Product Type A")
                .build();

        mockSyncResult = SyncResult.builder()
                .id(1L)
                .status(SyncStatus.SUCCESS)
                .totalContractsDownloaded(1)
                .totalAgreementsDownloaded(2)
                .totalAgreementsLoaded(2)
                .build();
    }

    @Test
    void executeFullSync_shouldSuccessfullyCompleteSync() {
        // Arrange
        when(externalRebateClient.fetchContracts(anyInt(), anyInt()))
                .thenReturn(Arrays.asList(mockContract))
                .thenReturn(new ArrayList<>());

        when(externalRebateClient.fetchAgreementById(anyString(), anyString()))
                .thenReturn(mockAgreement);

        when(rebateAgreementRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(rebateAgreementRepository.saveSyncResult(any(SyncResult.class)))
                .thenReturn(mockSyncResult);

        // Act
        SyncResult result = syncService.executeFullSync();

        // Assert
        assertNotNull(result);
        assertEquals(SyncStatus.SUCCESS, result.getStatus());
        verify(externalRebateClient, atLeastOnce()).fetchContracts(anyInt(), anyInt());
        verify(rebateAgreementRepository, times(1)).deleteAll();
        verify(rebateAgreementRepository, times(1)).saveAll(anyList());
        verify(rebateAgreementRepository, times(1)).saveSyncResult(any(SyncResult.class));
        verify(notificationService, times(1)).notifySuccess(any(SyncResult.class));
    }

    @Test
    void executeFullSync_shouldHandleExceptionAndNotifyFailure() {
        // Arrange
        when(externalRebateClient.fetchContracts(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("External service error"));

        when(rebateAgreementRepository.saveSyncResult(any(SyncResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertThrows(SyncException.class, () -> syncService.executeFullSync());

        verify(rebateAgreementRepository, times(1)).saveSyncResult(any(SyncResult.class));
        verify(notificationService, times(1)).notifyFailure(any(SyncResult.class));
    }

    @Test
    void executeSyncByContractId_shouldSuccessfullySyncSpecificContract() {
        // Arrange
        String contractId = "1527";

        when(externalRebateClient.fetchContractById(contractId))
                .thenReturn(mockContract);

        when(externalRebateClient.fetchAgreementById(anyString(), anyString()))
                .thenReturn(mockAgreement);

        when(rebateAgreementRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(rebateAgreementRepository.saveSyncResult(any(SyncResult.class)))
                .thenReturn(mockSyncResult);

        // Act
        SyncResult result = syncService.executeSyncByContractId(contractId);

        // Assert
        assertNotNull(result);
        assertEquals(SyncStatus.SUCCESS, result.getStatus());
        verify(externalRebateClient, times(1)).fetchContractById(contractId);
        verify(rebateAgreementRepository, times(1)).saveAll(anyList());
        verify(notificationService, times(1)).notifySuccess(any(SyncResult.class));
    }

    @Test
    void executeSyncByContractId_shouldHandleException() {
        // Arrange
        String contractId = "1527";

        when(externalRebateClient.fetchContractById(contractId))
                .thenThrow(new RuntimeException("Contract not found"));

        when(rebateAgreementRepository.saveSyncResult(any(SyncResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertThrows(SyncException.class, () -> syncService.executeSyncByContractId(contractId));

        verify(notificationService, times(1)).notifyFailure(any(SyncResult.class));
    }
}
