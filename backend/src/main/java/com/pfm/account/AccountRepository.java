package com.pfm.account;

import static com.pfm.config.csv.CsvIngParserConfig.IMPORT_TARGET_ACCOUNT_NAME;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

  @Query("select account from Account account where lower(account.name) like lower(:nameToFind) AND account.userId = :id")
  List<Account> findByNameIgnoreCaseAndUserId(@Param("nameToFind") String nameToFind, @Param("id") long id);

  List<Account> findByUserId(long userId);

  Optional<Account> findByIdAndUserId(long id, long userId);

  @Modifying(clearAutomatically = true)
  @Query("update Account account set account.balance = :newBalance where account.id = :id")
  void updateAccountBalance(@Param("newBalance") BigDecimal newBalance, @Param("id") long accountId);

  boolean existsByIdAndUserId(long accountId, long userId);

  default long getAccountIdByName(long userId) {
    return findByNameIgnoreCaseAndUserId(IMPORT_TARGET_ACCOUNT_NAME, userId).get(0).getId();
  }
}
