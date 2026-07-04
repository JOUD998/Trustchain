package com.joud.trustchain.campaign;


import com.joud.trustchain.campaign.dto.CampaignResponse;
import com.joud.trustchain.campaign.dto.CreateCampaignRequest;
import com.joud.trustchain.campaign.dto.UpdateCampaignRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;


    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public List<CampaignResponse> getAllCampaign(){
      return   campaignService.getAllCampaigns();
    }


    @PostMapping
    public CampaignResponse createCampaign(@Valid @RequestBody CreateCampaignRequest request) {
        return campaignService.createCampaign(request);

    }

    @GetMapping("/{id}")
    public CampaignResponse getCampaignById(@PathVariable Long id){
       return campaignService.getCampaignById(id);
    }

    @PutMapping("/{id}")
    public CampaignResponse updateCampaign(@PathVariable Long id,@RequestBody UpdateCampaignRequest request){
        return campaignService.updateCampaign(id,request);
    }

    @DeleteMapping("/{id}")
    public void deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
    }



}
