package com.joud.trustchain.blockchain_transaction;
import com.joud.trustchain.blockchain_transaction.dto.BlockchainTransactionResponse;
import com.joud.trustchain.blockchain_transaction.dto.BlockchainVerificationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blockchain-transactions")
public class BlockchainTransactionController {

    private final BlockchainTransactionService blockchainTransactionService;


    public BlockchainTransactionController(BlockchainTransactionService blockchainTransactionService) {
        this.blockchainTransactionService = blockchainTransactionService;
    }


    @GetMapping
    public List<BlockchainTransactionResponse> getAllBlockchainTransactions(){
        return   blockchainTransactionService.getAllBlockchainTransactions();
    }

    @GetMapping("/verify")
    public BlockchainVerificationResponse verifyBlockchainIntegrity(){
        return blockchainTransactionService.verifyBlockchainIntegrity();
    }


}
