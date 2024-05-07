package com.example.googledrivemerge.repository;

import com.example.googledrivemerge.pojo.MyUser;
import com.example.googledrivemerge.pojo.MyUserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyUserDataRepository extends JpaRepository<MyUserData, Long> {
    Optional<MyUserData> findByUser(MyUser user);
}