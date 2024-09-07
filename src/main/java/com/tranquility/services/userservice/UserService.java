package com.tranquility.services.userservice;

import com.tranquility.data.entities.User;
import com.tranquility.data.repositories.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    public void saveUser(User newUser) {
        User oldUser = userRepo.findByUsername(newUser.getUsername());
        if (oldUser != null) {
            oldUser.setUsername(newUser.getUsername());
            oldUser.setPassword(newUser.getPassword());
            userRepo.save(oldUser);
        } else {
            newUser.setRoles(new String[]{"USER"});
            userRepo.save(newUser);
        }
    }

    public List<User> getAll() {
        return userRepo.findAll();
    }

    public void deleteById(ObjectId id) {
        userRepo.deleteById(id);
    }

    public User findByUserName(String userName) {
        return userRepo.findByUsername(userName);
    }
}
