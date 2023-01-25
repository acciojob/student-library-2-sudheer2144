package com.driver.services;

import com.driver.models.Book;
import com.driver.models.Card;
import com.driver.models.Transaction;
import com.driver.models.TransactionStatus;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;


    @Value("${books.max_allowed}")
    public int max_allowed_books;

    @Value("${books.max_allowed_days}")
    public int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    public int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");
        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");
        //If the transaction is successful, save the transaction to the list of transactions and return the id

        //Note that the error message should match exactly in all cases
        Book book=bookRepository5.findById(bookId).get();
        Card card=cardRepository5.findById(cardId).get();

        Transaction transaction=new Transaction();
        transaction.setBook(book);
        transaction.setCard(card);
        transaction.setIssueOperation(true);

        if(!(bookRepository5.existsById(book.getId()) && book.isAvailable())){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new RuntimeException("Book is either unavailable or not present");
        }

        if(!(card.getCardStatus().toString().equals("ACTIVATED"))){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new RuntimeException("Card is invalid");
        }

        if(!(card.getBooks().size()>max_allowed_books)){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new RuntimeException("Book limit has reached for this card");
        }

        book.setAvailable(false);
        book.setCard(card);
        List<Book> cardBooks=card.getBooks();
        cardBooks.add(book);
        card.setBooks(cardBooks);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);

        bookRepository5.updateBook(book);
        transactionRepository5.save(transaction);

        return transaction.getTransactionId(); //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well

        //Making the book available for other Users
        Book book=transaction.getBook();
        book.setAvailable(true);
        book.setCard(null);
        bookRepository5.updateBook(book);

        Card card=transaction.getCard();

        //Calculating Fine amount
        Date issueDate = transaction.getTransactionDate();
        long timeIssueTime = Math.abs(System.currentTimeMillis() - issueDate.getTime());
        long no_of_days_passed = TimeUnit.DAYS.convert(timeIssueTime, TimeUnit.MILLISECONDS);
        int fine = 0;
        if(no_of_days_passed > getMax_allowed_days)
        {
            fine = (int)((no_of_days_passed - getMax_allowed_days) * fine_per_day);
        }

        //Creating returnBookTransaction
        Transaction returnBookTransaction=new Transaction();
        returnBookTransaction.setFineAmount(fine);
        returnBookTransaction.setBook(book);
        returnBookTransaction.setCard(card);
        returnBookTransaction.setIssueOperation(false);
        returnBookTransaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);

        transactionRepository5.save(returnBookTransaction);

        return returnBookTransaction; //return the transaction after updating all details
    }
}
