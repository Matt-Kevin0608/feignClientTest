package com.example.demo;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeignClientTest {


    @Test
    public void givenBookClient_shouldRunSuccessfully() throws Exception {
        BookClient bookClient = getBookClient();

        List<Book> books = bookClient.findAll().stream()
                .map(BookResource::getBook)
                .collect(Collectors.toList());

        assertTrue(books.size() > 2);
    }

    @Test
    public void givenBookClient_shouldFindOneBook() throws Exception {
        BookClient bookClient = getBookClient();
        Book book = bookClient.findByIsbn("0151072558").getBook();
        assertThat(book.getAuthor(), containsString("Orwell"));
    }

    @Test
    public void givenBookClient_shouldPostBook() throws Exception {
        BookClient bookClient = getBookClient();
        String isbn = UUID.randomUUID().toString();
        Book book = new Book(isbn, "Me", "It's me!", null, null);
        bookClient.create(book);
        book = bookClient.findByIsbn(isbn).getBook();

        assertThat(book.getAuthor(), is("Me"));
    }

    private BookClient getBookClient() {
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(BookClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BookClient.class, "http://localhost:8081/api/books");
    }
}
