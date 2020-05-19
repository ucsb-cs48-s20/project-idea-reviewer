package edu.ucsb.cs48.s20.demo.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucsb.cs48.s20.demo.entities.Student;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
    public List<Student> findByEmail(String email);
}