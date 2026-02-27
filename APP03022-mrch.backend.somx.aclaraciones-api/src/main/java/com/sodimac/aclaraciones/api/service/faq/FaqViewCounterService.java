package com.sodimac.aclaraciones.api.service.faq;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.aclaraciones.api.model.dto.view.FaqView;
import com.sodimac.aclaraciones.api.service.faq.command.impl.FaqQueryService;

/**
 * Incrementa el contador de vistas de una FAQ y devuelve la fila actualizada.
 */
@Service
public class FaqViewCounterService {

    private final NamedParameterJdbcTemplate jdbc;
    private final FaqQueryService queryService;

    public FaqViewCounterService(NamedParameterJdbcTemplate jdbc,
            FaqQueryService queryService) {
        this.jdbc = jdbc;
        this.queryService = queryService;
    }

    /**
     * Suma +1 a la columna <code>views</code> y devuelve la FAQ.
     *
     * @param id id de la FAQ
     * @return FaqView actualizada
     */
    @Transactional
    public FaqView findByIdAndIncrement(Long id) {
        String sql = "UPDATE public.faq SET views = views + 1 WHERE id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
        return queryService.findById(id);
    }
}
