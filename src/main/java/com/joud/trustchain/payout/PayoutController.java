package com.joud.trustchain.payout;

import com.joud.trustchain.payout.dto.CreatePayoutRequest;
import com.joud.trustchain.payout.dto.PayoutResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payouts")
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @GetMapping
    public List<PayoutResponse> getAllPayouts() {
        return payoutService.getAllPayouts();
    }

    @GetMapping("/milestone/{milestoneId}")
    public List<PayoutResponse> getPayoutsByMilestoneId(@PathVariable Long milestoneId) {
        return payoutService.getPayoutsByMilestoneId(milestoneId);
    }

    @PostMapping
    public PayoutResponse createPayout(@Valid @RequestBody CreatePayoutRequest request) {
        return payoutService.createPayout(request);
    }
}