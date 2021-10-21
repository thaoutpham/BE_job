package com.codegym.demo.controller;

import com.codegym.demo.model.Company;
import com.codegym.demo.model.Post;
import com.codegym.demo.service.company.ICompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/companies")
@CrossOrigin("*")
public class CompanyController {
    @Autowired
    ICompanyService companyService;

    @GetMapping
    public ResponseEntity<Iterable<Company>> findAll() {
        List<Company> companies = (List<Company>) companyService.findAll();
        if (companies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(companies, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Company> findById(@PathVariable Long id) {
        Optional<Company> company = companyService.findById(id);
        if (!company.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(company.get(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> edit(@PathVariable Long id, @RequestBody Company company) {
        Optional<Company> companyOptional = companyService.findById(id);
        if (!companyOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        if(company.getType().equals("")||company.getType() == null){
            company.setType(companyOptional.get().getType());
        }
        if(company.getPassword().trim().equals("")|| company.getPassword() == null){
            company.setPassword(companyOptional.get().getPassword());
        }
        company.setId(id);
        companyService.save(company);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/top")
    public ResponseEntity<Iterable<Company>> findTop() {
        Iterable<Company> company = companyService.findTopNew();
        return new ResponseEntity<>(company, HttpStatus.OK);
    }
    @GetMapping("/top/NumberOfStaff")
    public ResponseEntity<Iterable<Company>> findTopNumberOfStaff() {
        Iterable<Company> company = companyService.findTopNumberOfStaff();
        return new ResponseEntity<>(company, HttpStatus.OK);
    }
}
