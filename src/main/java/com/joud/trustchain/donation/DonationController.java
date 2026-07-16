package com.joud.trustchain.donation;
import com.joud.trustchain.donation.dto.DonationRequest;
import com.joud.trustchain.donation.dto.DonationResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION')")
    @GetMapping
    public List<DonationResponse> getAllDonations() {
        return donationService.getAllDonations();
    }

    @GetMapping("/campaign/{campaignId}")

    public List<DonationResponse> getDonationsByCampaignId(

            @PathVariable Long campaignId

    ) {

        return donationService.getDonationsByCampaignId(campaignId);

    }
}
