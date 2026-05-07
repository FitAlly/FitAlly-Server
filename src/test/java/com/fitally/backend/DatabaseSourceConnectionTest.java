package com.fitally.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class DatabaseSourceConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() throws Exception{
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("DB 연결 성공: " + connection.getMetaData().getURL());
        }
    }
}
