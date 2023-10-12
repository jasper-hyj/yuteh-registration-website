package com.yuteh.register.user;

import com.yuteh.register.form.Extractor;
import com.yuteh.register.user.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository("UserRepository")
public class JDBCUserRepository implements UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public JDBCUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User select(String email) {
        return jdbcTemplate.query(
                """
                        select * from user u
                        where u.email = ?
                        """,
                new Object[]{email}, new int[]{Types.VARCHAR},
                rs -> {
                    if (!rs.next()) return null;
                    return Extractor.user(rs);
                }
        );
    }

    @Override
    public List<User> selectList() {
        return jdbcTemplate.query(
                """
                        select * from user""",
                rs -> {
                    List<User> userList = new ArrayList<>();
                    while (rs.next()) {
                        userList.add(Extractor.user(rs));
                    }
                    return userList;
                }
        );
    }
}
