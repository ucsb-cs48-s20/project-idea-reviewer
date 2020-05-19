package edu.ucsb.cs48.s20.demo.repositories;

import edu.ucsb.cs48.s20.demo.entities.AppUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {
    public List<AppUser> findByEmail(String email);
}
