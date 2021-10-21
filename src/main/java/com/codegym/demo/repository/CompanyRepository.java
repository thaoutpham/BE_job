package com.codegym.demo.repository;

import com.codegym.demo.model.Company;
import com.codegym.demo.model.Post;
import com.codegym.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByEmail(String email); //Tim kiem email co ton tai trong DB khong?

    Boolean existsByEmail(String email); //email da co trong DB chua?

    Boolean existsByCompanyName(String companyName);
    @Query(value = "select * from company order by id desc limit 8", nativeQuery = true)
    Iterable<Company> findTopNew();
    @Query(value = "select * from company order by number_of_staff desc limit 6", nativeQuery = true)
    Iterable<Company> findTopNumberOfStaff();
}
