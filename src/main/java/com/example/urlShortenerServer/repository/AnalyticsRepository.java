package com.example.urlShortenerServer.repository;

import com.example.urlShortenerServer.domain.Analytic;
import com.example.urlShortenerServer.dto.DailyClicks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytic, Long>{
    @Query(value = """
    SELECT DATE (accessed_at) as date, COUNT(*) as clicks
    FROM analytics
    WHERE url_id = :urlId
    GROUP BY date
    ORDER BY date

""", nativeQuery = true)
    List<DailyClicks> getDailyClicks(@Param("urlId") Long urlId);

}
