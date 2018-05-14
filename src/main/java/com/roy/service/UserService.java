package com.roy.service;

import com.roy.dao.UserDAO;
import com.roy.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserService {

    @Autowired
    UserDAO userDAO;

    public User getById(int id){
        User user = userDAO.getById(id);
        return user;
    }


    public boolean tx(){
        User u1 = new User();
        u1.setId(3);
        u1.setName("33333");
        userDAO.insert(u1);

        User u2 = new User();
        u1.setId(2);
        u1.setName("22222");
        userDAO.insert(u2);
        return true;
    }
}
