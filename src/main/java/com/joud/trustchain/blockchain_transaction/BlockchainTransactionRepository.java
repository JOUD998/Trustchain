package com.joud.trustchain.blockchain_transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction,Long> {

    Optional<BlockchainTransaction> findTopByOrderByIdDesc();
}
