package com.pfm.transaction;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

  List<Transaction> findByUserId(long userId);

  Optional<Transaction> findByIdAndUserId(long transactionId, long userId);

  boolean existsByCategoryId(long categoryId);

  boolean existsByIdAndUserId(long transactionId, long userId);

  @Query("select new java.lang.String(transaction.internalId)"
      + "from Transaction transaction  where transaction.userId =:id and transaction.internalId is not null")
  Set<String> getAllInternalIds(@Param("id") Long userId);
}
