package com.joud.trustchain.voucher;

import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.CampaignRepository;
import com.joud.trustchain.security.CurrentUserService;
import com.joud.trustchain.user.UserRepository;
import com.joud.trustchain.voucher.dto.CreateVoucherRequest;
import com.joud.trustchain.voucher.dto.VerifyVoucherResponse;
import com.joud.trustchain.voucher.dto.VoucherResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final BlockchainTransactionService blockchainTransactionService;
    private final CurrentUserService currentUserService;

    public VoucherService(VoucherRepository voucherRepository, CampaignRepository campaignRepository, UserRepository userRepository, BlockchainTransactionService blockchainTransactionService, CurrentUserService currentUserService) {
        this.voucherRepository = voucherRepository;
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.blockchainTransactionService = blockchainTransactionService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {

        Voucher voucher = Voucher.builder()
                .code(generateUniqueCode())
                .amount(request.getAmount())
                .status(VoucherStatus.ISSUED)
                .issuedAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .campaign(campaignRepository.getById(request.getCampaignId()))
                .beneficiary(userRepository.getById(request.getBeneficiaryId()))
                .build();

        voucher = voucherRepository.save(voucher);
        Long currentUserId = currentUserService.getCurrentUser().getId();

        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.VOUCHER_ISSUED,
                BlockchainEntityType.VOUCHER,
                voucher.getId(),
                "Voucher " + voucher.getCode()
                        + " was issued for beneficiary " + request.getBeneficiaryId()
                        + " from campaign " + request.getCampaignId(),
                currentUserId
        );

        return mapToVoucherResponse(voucher);
    }

    public VerifyVoucherResponse verifyVoucher(String code) {

        VerifyVoucherResponse response = new VerifyVoucherResponse();
        response.setCode(code);

        Optional<Voucher> optionalVoucher = voucherRepository.findByCode(code);

        if (optionalVoucher.isEmpty()) {
            response.setValid(false);
            response.setMessage("Voucher not found");
            return response;
        }

        Voucher voucher = optionalVoucher.get();

        response.setStatus(voucher.getStatus());
        response.setExpiresAt(voucher.getExpiresAt());

        if (voucher.getStatus() == VoucherStatus.CANCELLED) {
            response.setValid(false);
            response.setMessage("Voucher is cancelled");
            return response;
        }

        if (voucher.getStatus() == VoucherStatus.REDEEMED) {
            response.setValid(false);
            response.setMessage("Voucher is already used");
            return response;
        }

        if (voucher.getExpiresAt().isBefore(LocalDateTime.now())) {
            response.setValid(false);
            response.setMessage("Voucher is expired");
            return response;
        }

        response.setValid(true);
        response.setMessage("Voucher is valid");
        return response;
    }


    @Transactional
    public VoucherResponse redeemVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (voucher.getStatus() == VoucherStatus.REDEEMED) {
            throw new RuntimeException("Voucher is already used");
        }

        if (voucher.getStatus() == VoucherStatus.CANCELLED) {
            throw new RuntimeException("Voucher is cancelled");
        }

        if (voucher.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Voucher is expired");
        }

        voucher.setStatus(VoucherStatus.REDEEMED);
        voucher.setRedeemedAt(LocalDateTime.now());

        voucher = voucherRepository.save(voucher);
        Long currentUserId = currentUserService.getCurrentUser().getId();

        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.VOUCHER_REDEEMED,
                BlockchainEntityType.VOUCHER,
                voucher.getId(),
                "Voucher " + voucher.getCode()
                        + " was redeemed by user " + currentUserId
                        + " for beneficiary " + voucher.getBeneficiary().getId()
                        + " from campaign " + voucher.getCampaign().getId(),
                currentUserId

        );


        return mapToVoucherResponse(voucher);
    }


    private String generateUniqueCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();

        String code;

        do {
            StringBuilder randomPart = new StringBuilder();

            for (int i = 0; i < 8; i++) {
                int index = random.nextInt(characters.length());
                randomPart.append(characters.charAt(index));
            }

            code = "VCH-" + randomPart;

        } while (voucherRepository.existsByCode(code));

        return code;


    }

    private VoucherResponse mapToVoucherResponse(Voucher voucher) {
        VoucherResponse voucherResponse = new VoucherResponse();
        voucherResponse.setId(voucher.getId());
        voucherResponse.setCode(voucher.getCode());
        voucherResponse.setAmount(voucher.getAmount());
        voucherResponse.setStatus(voucher.getStatus());
        voucherResponse.setIssuedAt(voucher.getIssuedAt());
        voucherResponse.setExpiresAt(voucher.getExpiresAt());
        voucherResponse.setRedeemedAt(voucher.getRedeemedAt());
        voucherResponse.setCampaignTitle(voucher.getCampaign().getTitle());
        voucherResponse.setCampaignId(voucher.getCampaign().getId());
        voucherResponse.setBeneficiaryFullName(voucher.getBeneficiary().getFullName());
        voucherResponse.setBeneficiaryId(voucher.getBeneficiary().getId());
        return voucherResponse;

    }


}
