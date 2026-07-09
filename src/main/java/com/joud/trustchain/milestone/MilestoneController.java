package com.joud.trustchain.milestone;


import com.joud.trustchain.milestone.dto.CreateMilestoneRequest;
import com.joud.trustchain.milestone.dto.MilestoneResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")

public class MilestoneController {

    private final MilestoneService milestoneService;


    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping
    public List<MilestoneResponse> getAllMilestones(){
       return milestoneService.getAllMilestones();
    }

    @GetMapping("/campaign/{campaignId}")
    public List<MilestoneResponse> getMilestonesByCampaignId(@PathVariable Long campaignId){
        return milestoneService.getMilestonesByCampaignId(campaignId);
    }



    @PostMapping
    public MilestoneResponse createMilestone(@Valid @RequestBody CreateMilestoneRequest request){
        return milestoneService.createMilestone(request);

    }






}
