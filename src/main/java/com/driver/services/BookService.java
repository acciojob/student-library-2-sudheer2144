package com.driver.services;

import com.driver.models.Author;
import com.driver.models.Book;
import com.driver.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {


    @Autowired
    BookRepository bookRepository2;

    public void createBook(Book book){
        bookRepository2.save(book);
    }

    public List<Book> getBooks(String genre, boolean available, String author){
        //List<Book> books = bookRepository2.findBooksByGenreAuthor(genre,author,true); //find the elements of the list by yourself
        List<Book> books;
        if(genre!=null&&author!=null){
            books = bookRepository2.findBooksByGenreAuthor(genre,author,available);
        }
        else if(genre==null){
            books=bookRepository2.findBooksByAuthor(author,available);
        }
        else if(author==null){
            books=bookRepository2.findBooksByGenre(genre,available);
        }
        else{
            books=bookRepository2.findByAvailability(available);
        }
        return books;
    }

}
