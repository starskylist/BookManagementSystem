package com.demo.geode.repository;

import com.demo.geode.model.BookGemfire;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.gemfire.repository.query.annotation.Trace;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface BookRepositoryGemfire extends CrudRepository<BookGemfire,String> {

    @Query("SELECT * FROM /BookGemfire p WHERE p.user = $1")
    Collection<BookGemfire> findByUser(String user);

    @Trace
    Collection<BookGemfire> findByIsbn(String isbn);
    @Trace
    @Query("SELECT * FROM /BookGemfire p WHERE p.bookname LIKE $1")
    Collection<BookGemfire> findByBooknameLike(String bookname);

//    @Query("SELECT * FROM /BookGemfire p WHERE p.email LIKE $1")
//    Collection<BookGemfire> getPersonsByEmailLike(String email);

    @Query("SELECT * FROM /BookGemfire limit $1")
    Collection<BookGemfire> describle(int rows);

    @Query("SELECT COUNT(*) FROM /BookGemfire")
    Integer countAll();

    @Trace
    void deleteById(String id);
}
