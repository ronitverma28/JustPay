package app.wallet.smart_wallet.util;

import app.wallet.smart_wallet.dto.response.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import app.wallet.smart_wallet.entity.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EntityMapper {

    public static UserResponse toUserSummary(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    public static PoolResponse toPoolResponse(Pool pool) {
        List<UserResponse> members = pool.getMembers()
            .stream()
            .sorted(Comparator.comparing(User::getId))
            .map(EntityMapper::toUserSummary)
            .collect(Collectors.toList());

        return PoolResponse.builder()
            .id(pool.getId())
            .name(pool.getName())
            .createdBy(toUserSummary(pool.getCreatedBy()))
            .members(members)
            .poolBalance(pool.getPoolBalance())
            .createdAt(pool.getCreatedAt())
            .build();
    }

    public static WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
            .balance(wallet.getBalance())
            .updatedAt(wallet.getUpdatedAt())
            .build();
    }

    public static ExpenseResponse toExpenseResponse(Expense expense) {
        List<ExpenseSplitResponse> splitResponses = expense.getSplits()
            .stream()
            .sorted(Comparator.comparing(ExpenseSplit::getId))
            .map(split -> ExpenseSplitResponse.builder()
                .userId(split.getUser().getId())
                .userName(split.getUser().getName())
                .shareAmount(split.getShareAmount())
                .status(split.getStatus())
                .build())
            .collect(Collectors.toList());

        return ExpenseResponse.builder()
            .id(expense.getId())
            .poolId(expense.getPool().getId())
            .poolName(expense.getPool().getName())
            .createdBy(expense.getCreatedBy().getId())
            .createdByName(expense.getCreatedBy().getName())
            .amount(expense.getAmount())
            .description(expense.getDescription())
            .splitType(expense.getSplitType())
            .status(expense.getStatus())
            .createdAt(expense.getCreatedAt())
            .splits(splitResponses)
            .build();
    }

    public static TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .type(transaction.getType())
            .status(transaction.getStatus())
            .referenceId(transaction.getReferenceId())
            .poolId(transaction.getPool() != null ? transaction.getPool().getId() : null)
            .poolName(transaction.getPool() != null ? transaction.getPool().getName() : null)
            .createdAt(transaction.getCreatedAt())
            .build();
    }
}
