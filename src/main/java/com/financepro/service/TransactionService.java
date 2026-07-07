package com.financepro.service;

import com.financepro.dto.PagedResult;
import com.financepro.dto.TransactionDTO;
import com.financepro.entity.Transaction;
import com.financepro.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Transaction listing, filtering, and CSV export.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public PagedResult<Transaction> getFiltered(Long userId, String type, String category,
                                                String search, int page, int size) {
        return transactionRepository.findFiltered(userId, type, category, search, page, size);
    }

    public List<Transaction> getRecentByUser(Long userId) {
        return transactionRepository.findTop10ByUserIdOrderByTransactionDateDesc(userId);
    }

    public List<Transaction> getAllByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
    }

    public TransactionDTO toDTO(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .transactionDate(t.getTransactionDate())
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt())
                .build();
    }

    public List<TransactionDTO> toDTOList(List<Transaction> list) {
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
