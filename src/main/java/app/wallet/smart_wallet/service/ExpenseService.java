package app.wallet.smart_wallet.service;

import app.wallet.smart_wallet.dto.request.CreateExpenseRequest;
import app.wallet.smart_wallet.dto.response.ExpenseResponse;

import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(CreateExpenseRequest request);

    List<ExpenseResponse> getExpensesByPool(Long poolId);
}