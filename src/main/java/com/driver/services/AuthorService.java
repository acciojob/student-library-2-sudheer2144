package com.driver.services;

import com.driver.models.Author;
import com.driver.models.Book;
import com.driver.models.CardStatus;
import com.driver.repositories.AuthorRepository;
import com.driver.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {


    @Autowired
    AuthorRepository authorRepository1;
    public void create(Author author){
        authorRepository1.save(author);
    }
}
