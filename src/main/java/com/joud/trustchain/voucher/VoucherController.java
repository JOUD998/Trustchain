package com.joud.trustchain.voucher;
import com.joud.trustchain.voucher.dto.CreateVoucherRequest;
import com.joud.trustchain.voucher.dto.VerifyVoucherResponse;
import com.joud.trustchain.voucher.dto.VoucherResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping
    public VoucherResponse createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        return voucherService.createVoucher(request);
    }


    @GetMapping("/verify/{code}")
    public VerifyVoucherResponse verifyVoucher(@PathVariable String code){
        return voucherService.verifyVoucher(code);

    }

    @PostMapping("/{code}/redeem")
    public VoucherResponse redeemVoucher(@PathVariable String code) {
        return voucherService.redeemVoucher(code);
    }
}