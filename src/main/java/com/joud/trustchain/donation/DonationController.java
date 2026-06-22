package com.joud.trustchain.donation;
import com.joud.trustchain.donation.dto.DonationRequest;
import com.joud.trustchain.donation.dto.DonationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;
    public DonationController(DonationService donationService){
        this.donationService = donationService;
    }

    @PostMapping
    public DonationResponse createDonation(@Valid @RequestBody DonationRequest request) {
        return donationService.createDonation(request);

    }

}
