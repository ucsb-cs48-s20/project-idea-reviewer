package edu.ucsb.cs48.s20.demo.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucsb.cs48.s20.demo.entities.Admin;

@Repository
public interface AdminRepository extends CrudRepository<Admin, Long> {
    public List<Admin> findByEmail(String email);
}