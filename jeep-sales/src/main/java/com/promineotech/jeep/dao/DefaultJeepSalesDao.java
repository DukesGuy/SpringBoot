package com.promineotech.jeep.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DefaultJeepSalesDao implements JeepSalesDao {
	private static final String MODELS_TABLE = "models";
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Override 
	public List<Jeep> fetchJeeps(JeepModel model, String trim) {
		log.info("DAO: model={}, trim={}", model, trim);
		
		// @formatter:off
		String sql = ""
				+ "SELECT * "
				+ "FROM " + MODELS_TABLE + " "
				+ "WHERE model_id = :model_id "
				+ "AND trim_level = :trim_level";
		// @formatter:on
		
		Map<String,Object> parms = Map.of("model_id", model.toString(), "trim_level", trim);
		
		return jdbcTemplate.query(sql, parms, new RowMapper<Jeep>() {
			@Override
			public Jeep mapRow(ResultSet rs, int rowNum) throws SQLException {
				// @formatter:off
				return Jeep.builder()
						.basePrice(rs.getBigDecimal("base_price"))
						.model(JeepModel.valueOf(rs.getString("model_id")))
						.modelPK(rs.getObject("model_pk", Long.class))
						.numDoors(rs.getInt("num_doors"))
						.trimLevel(rs.getString("trim_level"))
						.wheelSize(rs.getInt("wheel_size"))
						.build();
				// @formatter:on
			}});
		}

}
