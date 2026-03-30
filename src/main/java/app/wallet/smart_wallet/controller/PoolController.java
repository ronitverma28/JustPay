package app.wallet.smart_wallet.controller;

import com.smartwallet.dto.AddMemberRequest;
import com.smartwallet.dto.ApiResponse;
import com.smartwallet.dto.CreatePoolRequest;
import com.smartwallet.dto.PoolResponse;
import com.smartwallet.service.PoolService;
import com.smartwallet.util.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pools")
@RequiredArgsConstructor
public class PoolController {

    private final PoolService poolService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PoolResponse>> createPool(@Valid @RequestBody CreatePoolRequest request) {
        PoolResponse response = poolService.createPool(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseUtil.success("Pool created successfully", response));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PoolResponse>> addMember(
        @PathVariable("id") Long poolId,
        @Valid @RequestBody AddMemberRequest request
    ) {
        PoolResponse response = poolService.addMember(poolId, request.getUserId());
        return ResponseEntity.ok(ApiResponseUtil.success("Member added successfully", response));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PoolResponse>> removeMember(
        @PathVariable("id") Long poolId,
        @PathVariable Long userId
    ) {
        PoolResponse response = poolService.removeMember(poolId, userId);
        return ResponseEntity.ok(ApiResponseUtil.success("Member removed successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PoolResponse>> getPoolDetails(@PathVariable("id") Long poolId) {
        PoolResponse response = poolService.getPoolDetails(poolId);
        return ResponseEntity.ok(ApiResponseUtil.success("Pool details fetched", response));
    }
}
