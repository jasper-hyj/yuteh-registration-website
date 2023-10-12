package com.yuteh.register.admin;

import com.yuteh.register.form.Extractor;
import com.yuteh.register.admin.model.analysis.OptionCount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AnalysisRepository {
    private final JdbcTemplate jdbcTemplate;

    public AnalysisRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OptionCount> select(String formId, String id, String locale) {
        return jdbcTemplate.query(
                """
                        select
                        json_unquote(json_extract(fd.`data`, concat('$."' , ?, '"'))) as "option_count_id",
                        ot.name as "option_count_name"
                        ,count(*) as "option_count_count"
                        from form_data fd, option_trans ot
                        where ot.id = json_unquote(json_extract(fd.`data`,concat('$."' , ?, '"')))
                        and ot.lang = ?
                        and fd.form_id = ?
                        group by json_unquote(json_extract(fd.`data`, concat('$."' , ?, '"')))
                        """,
                new Object[] {id, id, locale, formId, id}, new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR},
                rs -> {
                    List<OptionCount> optionCountList = new ArrayList<>();
                    while (rs.next()) {
                        optionCountList.add(Extractor.optionCount(rs));
                    }
                    return optionCountList;
                }
        );
    }
}
