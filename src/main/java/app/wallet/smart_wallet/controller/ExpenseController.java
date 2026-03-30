package app.wallet.smart_wallet.controller;

import com.smartwallet.dto.ApiResponse;
import com.smartwallet.dto.CreateExpenseRequest;
import com.smartwallet.dto.ExpenseResponse;
import com.smartwallet.service.ExpenseService;
import com.smartwallet.util.ApiResponseUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseUtil.success("Expense created successfully", response));
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getPoolExpenses(@PathVariable Long poolId) {
        List<ExpenseResponse> response = expenseService.getExpensesByPool(poolId);
        return ResponseEntity.ok(ApiResponseUtil.success("Expenses fetched successfully", response));
    }
}
